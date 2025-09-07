package org.schema.schine.network.udp;

public class UDPException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private Exception ioException;

	public UDPException(String string) {
		super(string);
	}

	public UDPException(String string, Exception e) {
		super(string);
		this.ioException = e;
	}

	/**
	 * @return the ioException
	 */
	public Exception getIoException() {
		return ioException;
	}

}
