package com.jmb.shorturl.datasource;

import org.apache.log4j.Logger;

import com.jmb.shorturl.model.Url;
import com.jmb.shorturl.util.Base62Encoder;
import com.jmb.shorturl.exception.UrlException;
import com.jmb.shorturl.exception.UrlNotFoundException;

public abstract class AbstractDatasource {

	public abstract Url getUrl(int urlId) throws UrlNotFoundException;

	public Url getUrl(String shortUrl) throws UrlNotFoundException {
		return getUrl(shortUrl.toCharArray());
	}

	public Url getUrl(char[] shortUrl) throws UrlNotFoundException {
		return getUrl(new Base62Encoder().decode(shortUrl));
	}

}