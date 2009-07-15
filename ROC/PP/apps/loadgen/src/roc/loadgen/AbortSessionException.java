package roc.loadgen;

public class AbortSessionException extends Exception {

	/**
	 * 
	 */
	public AbortSessionException() {
		super();
	}

	/**
	 * @param message
	 */
	public AbortSessionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AbortSessionException(Throwable cause) {
		super(cause);
	}

	public AbortSessionException(String s, Throwable t) {
		super(s, t);
	}

}
