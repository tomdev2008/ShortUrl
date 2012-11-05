package com.jmb.shorturl.exception;

public class ShortUrlException extends Exception {

	public ShortUrlException() {}

	public ShortUrlException(String message) {
		super(message);
	}

	public ShortUrlException(Throwable cause) {
		super(cause);
	}

	public ShortUrlException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShortUrlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
