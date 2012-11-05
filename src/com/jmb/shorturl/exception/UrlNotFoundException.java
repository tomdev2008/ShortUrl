package com.jmb.shorturl.exception;

public class UrlNotFoundException extends ShortUrlException {

	public UrlNotFoundException() {}

	public UrlNotFoundException(String message) {
		super(message);
	}

	public UrlNotFoundException(Throwable cause) {
		super(cause);
	}

	public UrlNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public UrlNotFoundException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
