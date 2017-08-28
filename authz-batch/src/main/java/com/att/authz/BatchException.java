/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz;

public class BatchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3877245367723491192L;

	public BatchException() {
	}

	public BatchException(String message) {
		super(message);
	}

	public BatchException(Throwable cause) {
		super(cause);
	}

	public BatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
