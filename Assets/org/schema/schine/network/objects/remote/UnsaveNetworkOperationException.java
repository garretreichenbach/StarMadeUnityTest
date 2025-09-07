package org.schema.schine.network.objects.remote;

public class UnsaveNetworkOperationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public UnsaveNetworkOperationException() {
		super();
	}

	// #RM1958 remove Java8 constructor
//	public UnsaveNetworkOperationException(String arg0, Throwable arg1,
//			boolean arg2, boolean arg3) {
//		super(arg0, arg1, arg2, arg3);
//	}

	public UnsaveNetworkOperationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UnsaveNetworkOperationException(String arg0) {
		super(arg0);
	}

	public UnsaveNetworkOperationException(Throwable arg0) {
		super(arg0);
	}

}
