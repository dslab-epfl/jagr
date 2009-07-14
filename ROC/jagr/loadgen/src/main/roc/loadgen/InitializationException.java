package roc.loadgen;

public class InitializationException extends Exception {

	/**
	 * 
	 */
	public InitializationException() {
		super();
	}

	/**
	 * @param message
	 */
	public InitializationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InitializationException(Throwable cause) {
		super(cause);
	}

	public InitializationException(String s, Throwable t) {
		super(s, t);
	}

}
