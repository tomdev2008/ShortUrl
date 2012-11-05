package com.jmb.shorturl.model;

import org.apache.log4j.Logger;

import com.jmb.shorturl.util.Base62Encoder;

public class Url {

	private final char[] shortUrl;
	private final char[] longUrl;
	private final int urlId;

	public Url(int urlId, char[] shortUrl, char[] longUrl) {
		this.shortUrl = shortUrl;
		this.urlId = urlId;
		this.longUrl = longUrl;
	}

	public Url(int urlId, String shortUrl, String longUrl) {
		this.urlId = urlId;
		this.shortUrl = shortUrl.toCharArray();
		this.longUrl = longUrl.toCharArray();
	}

	public Url(int urlId, String longUrl) {
		this.urlId = urlId;
		this.shortUrl = new Base62Encoder().encode(urlId);
		this.longUrl = longUrl.toCharArray();
	}

	public Url(int urlId, char[] longUrl) {
		this.urlId = urlId;
		this.shortUrl = new Base62Encoder().encode(urlId);
		this.longUrl = longUrl;
	}

	public Url(char[] shortUrl, char[] longUrl) {
		this.shortUrl = shortUrl;
		this.urlId = new Base62Encoder().decode(shortUrl);
		this.longUrl = longUrl;
	}

	public Url(String shortUrl, char[] longUrl) {
		this.shortUrl = shortUrl.toCharArray();
		this.urlId = new Base62Encoder().decode(shortUrl);
		this.longUrl = longUrl;
	}

	@Override
	public String toString() {
		return "Url [shortUrl=" + String.valueOf(shortUrl) + ", longUrl="
				+ String.valueOf(longUrl) + ", urlId=" + urlId + "]";
	}

	public String toJson(){
		return "{\"longUrl\": " + longUrl + ", \"shortUrl\": " + shortUrl + "}";
	}

	@Override
	public int hashCode() {
		return this.urlId;
	}

	@Override
	public boolean equals(Object obj) {
		Url url = (Url) obj;
		if (this.urlId != url.urlId) {
			return false;
		}
		if (!this.longUrl.equals(url.longUrl)) {
			return false;
		}
		if (!this.shortUrl.equals(url.shortUrl)) {
			return false;
		}
		return true;
	}

	public char[] getShortUrl() {
		return shortUrl;
	}

	public char[] getLongUrl() {
		return longUrl;
	}

	public int getUrlId() {
		return urlId;
	}

	public String getShortUrlString() {
		return String.valueOf(shortUrl);
	}

	public String getLongUrlString() {
		return String.valueOf(longUrl);
	}

}