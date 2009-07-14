package roc.loadgen;

import java.util.*;

/**
 * defines arguments for interceptors/sessions used by loadgen
 *
 */
public class Arg {

	public static final int ARG_BOOLEAN = 0;
	public static final int ARG_STRING = 1;
	public static final int ARG_INTEGER = 2;
	public static final int ARG_DOUBLE = 3;
	public static final int ARG_LIST = 4;
	public static final int ARG_MAP = 5;

	public String name;
	public String description;
	public int argType; // choose from one of the ARG_* above.
	public boolean required;
	public String defaultValue;

	public Arg(
		String name,
		String description,
		int argType,
		boolean required,
		String defaultValue) {
		this.name = name;
		this.description = description;
		this.argType = argType;
		this.required = required;
		this.defaultValue = defaultValue;
	}

	Object parseArgument(String s, Engine engine) throws ArgException {

		if (s != null && s.startsWith("$")) {
			String key = s.substring(1);
			s = (String) engine.getAttr(key);
			if( required && s == null ) {
			    throw new ArgException( "Required command-line argument '" + key + "' was not specified" );
			}
		}

		if (required && s == null)
			throw new ArgException(
				"Required argument '" + name + "' not declared!");

		if (s == null)
			s = defaultValue;

		if (argType == ARG_BOOLEAN) {
			return Boolean.valueOf(s);
		} else if (argType == ARG_STRING) {
			return s;
		} else if (argType == ARG_INTEGER) {
			return new Integer(s);
		} else if (argType == ARG_DOUBLE) {
			return new Double(s);
		} else if (argType == ARG_LIST) {
			List ret = new LinkedList();
			StringTokenizer st = new StringTokenizer(s, ",\n");
			while (st.hasMoreTokens()) {
				String t = st.nextToken();
				t = t.trim();
				if (t.length() != 0)
					ret.add(t);
			}
			return ret;
		} else if (argType == ARG_MAP) {
			Map ret = new HashMap();
			StringTokenizer st = new StringTokenizer(s, ",\n");
			while (st.hasMoreTokens()) {
				String t = st.nextToken();
				int i = t.indexOf('=');
				String k = t.substring(0, i);
				String v = t.substring(i + 1);
				k = k.trim();
				v = v.trim();
				ret.put(k, v);
			}
			return ret;
		} else {
			throw new ArgException(
				"PluginArg: while parsing argument '"
					+ name
					+ "', unrecognized argument type: "
					+ argType);
		}

	}
}
