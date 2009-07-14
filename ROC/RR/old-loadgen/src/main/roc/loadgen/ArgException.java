/*
 * Created on Apr 17, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package roc.loadgen;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ArgException extends Exception {

	/**
	 * 
	 */
	public ArgException() {
		super();
	}

	/**
	 * @param message
	 */
	public ArgException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArgException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ArgException(Throwable cause) {
		super(cause);
	}

}
