package life.qbic.dss.registration.usecases

import life.qbic.datamodel.samples.OpenbisTestSample

import java.nio.file.Path

interface DataSetRegistrationDataSource {

    String createNewTestSample(String parentBioSampleCode, String type)

    String createNewAnalysisRunSample(String parentTestSampleCode)

    String determineSampleType(String sampleCode)

    OpenbisTestSample findTestSample(String sampleCode)

    String registerDataSet(List<Path> dataSetFiles, String runSampleCode)

}