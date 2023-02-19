package org.mskcc.cbio.portal.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.DatabaseProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Data source that self-initializes based on cBioPortal configuration.
 */
public class JdbcDataSource extends BasicDataSource {

    public JdbcDataSource () {
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();

        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String mysqlDriverClassName = dbProperties.getDbDriverClassName();
        String database = dbProperties.getDbName();
        String useSSL = (!StringUtils.isBlank(dbProperties.getDbUseSSL())) ? dbProperties.getDbUseSSL() : "false";
        String enablePooling = (!StringUtils.isBlank(dbProperties.getDbEnablePooling())) ? dbProperties.getDbEnablePooling(): "false";
        String connectionURL = dbProperties.getConnectionURL();
        
        Assert.hasText(userName, errorMessage("username", "db.user"));
        Assert.hasText(password, errorMessage("password", "db.password"));
        Assert.hasText(mysqlDriverClassName, errorMessage("driver class name", "db.driver"));
        
        Assert.isTrue(
            !((defined(host) || defined(database) || defined(dbProperties.getDbUseSSL())) && defined(connectionURL)),
            "The portal.properties file defines both db.connection_string and (one of) db.host, " +
             "db.portal_db_name and db.use_ssl. Please configure with either db.connection_string (preferred), " + 
             "or db.host, db.portal_db_name and db.use_ssl."
        );
        
        // For backward compatibility, build connection URL from individual properties.
        if (connectionURL == null) {
            Assert.hasText(host, errorMessage("host", "db.host") + 
                " Or preferably, set the 'db.connection_string' and remove 'db.host', 'db.portal_db_name' and 'db.use_ssl' (best practice).");
            Assert.hasText(database, errorMessage("database name", "db.portal_db_name") + 
                " Or preferably, set the 'db.connection_string' and remove 'db.host', 'db.portal_db_name' and 'db.use_ssl (best practice).");
            System.out.println("\n----------------------------------------------------------------------------------------------------------------");
            System.out.println("-- Deprecation warning:");
            System.out.println("-- You are connection to the database using the deprecated 'db.host', 'db.portal_db_name' and 'db.use_ssl' properties.");
            System.out.println("-- Please use the 'db.connection_string' instead (see https://docs.cbioportal.org/deployment/customization/portal.properties-reference/).");
            System.out.println("----------------------------------------------------------------------------------------------------------------\n");
            connectionURL = String.format(
                "jdbc:mysql://%s/%s?zeroDateTimeBehavior=convertToNull&useSSL=%s",
                host, database, useSSL
            );
        }
        
        this.setUrl(connectionURL);

        //  Set up poolable data source
        this.setDriverClassName(mysqlDriverClassName);
        this.setUsername(userName);
        this.setPassword(password);
        // Disable this to avoid caching statements
        this.setPoolPreparedStatements(Boolean.valueOf(enablePooling));
        // these are the values cbioportal has been using in their production
        // context.xml files when using jndi
        this.setMaxTotal(500);
        this.setMaxIdle(30);
        this.setMaxWaitMillis(10000);
        this.setMinEvictableIdleTimeMillis(30000);
        this.setTestOnBorrow(true);
        this.setValidationQuery("SELECT 1");
        this.setJmxName("org.cbioportal:DataSource=" + database);
    }
    
    private String errorMessage(String displayName, String propertyName) {
        return String.format("No %s provided for database connection. Please set '%s' in portal.properties.", displayName, propertyName);
    }
    
    private boolean defined(String property) {
        return property != null && !property.isEmpty();
    }
}
