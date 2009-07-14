package se.jadestone.util.http;

import org.mortbay.http.*;

/**
* A Jetty request log which does not log requests depending
* on the name of the requested file.
*
* @author Martin Vilcans (martin@jadestone.se)
*/
public class LimitedNCSARequestLog extends NCSARequestLog {

	/**
	 * Do not log requests for this path/these paths.
	 */
	 private String _ignorePath;

	/**
	 * A PathMap with keys containing the paths in _ignorePath.
	 * I use the PathMap as a set, since there is no PathSet.
	 */
	private PathMap _ignorePathMap;

	public LimitedNCSARequestLog() throws java.io.IOException {}

	public LimitedNCSARequestLog(String filename) throws java.io.IOException {
		super(filename);
	}

	/**
	* Set which paths to ignore.
	*
	* @param ignorePath The path to ignore, e.g. "*.gif,*.jpeg,/images/*"
	*/
	public void setIgnorePath(String ignorePath) {
		_ignorePath = ignorePath;

		if(ignorePath == null) {
			_ignorePathMap = null;
		}
		else {
			_ignorePathMap = new PathMap();
			_ignorePathMap.put(ignorePath, "dummy");
		}
	}

	public String getIgnorePath() {
		return _ignorePath;
	}

	public void log(org.mortbay.http.HttpRequest request, org.mortbay.http.HttpResponse response, int responseLength) {
		if(_ignorePathMap == null || _ignorePathMap.getMatch(request.getPath()) == null) {
			super.log(request, response, responseLength);
		}
	}
}
