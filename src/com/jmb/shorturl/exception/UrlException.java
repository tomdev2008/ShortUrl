package com.jmb.shorturl.exception;

public class UrlException extends ShortUrlException {

	public UrlException() {}

	public UrlException(String message) {
		super(message);
	}

	public UrlException(Throwable cause) {
		super(cause);
	}

	public UrlException(String message, Throwable cause) {
		super(message, cause);
	}

	public UrlException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
