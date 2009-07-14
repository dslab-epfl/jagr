package roc.pinpoint.analysis;

import java.util.Map;

/**
 * Analysis engine plugins must implement this interface.
 * 
 * @author emrek
 *
 */
public interface Plugin {

    PluginArg[] getPluginArguments();

    /**
     * initialize and start the plugin.  called by analysisengine when the
     * plugin is  loaded.
     * @param id  the identifier of plugin interface
     * @param args arguments being passed top the plugin
     * @param engine  a reference to the analysis engine 
     * @throws PluginException a problem occurred while starting the plugin
     */
    void start(String id, Map args, AnalysisEngine engine)
        throws PluginException;

    /**
     * stop the plugin.  called by analysisengine when plugin is being unloaded.
     * @throws PluginException a problem occurred stopping the plugin
     */
    void stop() throws PluginException;

}
