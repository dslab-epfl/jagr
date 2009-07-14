package roc.loadgen;

public class AbortRequestException extends Exception {

	/**
	 * 
	 */
	public AbortRequestException() {
		super();
	}

	/**
	 * @param message
	 */
	public AbortRequestException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AbortRequestException(Throwable cause) {
		super(cause);
	}

	public AbortRequestException(String s, Throwable t) {
		super(s, t);
	}

}
