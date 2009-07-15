/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis;

// marked for release 1.0

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
