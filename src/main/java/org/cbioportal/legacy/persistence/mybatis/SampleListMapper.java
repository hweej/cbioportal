package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.SampleListToSampleId;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface SampleListMapper {
    
    List<SampleList> getAllSampleLists(List<String> studyIds, String projection, Integer limit, Integer offset, String sortBy, 
                                       String direction);
    
    BaseMeta getMetaSampleLists(String studyId);

    SampleList getSampleList(String sampleListId, String projection);

    List<SampleList> getSampleLists(List<String> sampleListIds, String projection);

    List<String> getAllSampleIdsInSampleList(String sampleListId);

    List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds);
}
