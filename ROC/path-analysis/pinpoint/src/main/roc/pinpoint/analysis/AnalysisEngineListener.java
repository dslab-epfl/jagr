package roc.pinpoint.analysis;

interface AnalysisEngineListener {

    void pluginLoaded( AnalysisEngine engine, String id );

    void pluginUnloaded( AnalysisEngine engine, String id );

    void recordCollectionCreated( AnalysisEngine engine, String id );

    void recordCollectionRemoved( AnalysisEngine engine, String id );

}
