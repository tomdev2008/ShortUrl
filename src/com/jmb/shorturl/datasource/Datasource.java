package com.jmb.shorturl.datasource;

import org.apache.log4j.Logger;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalNotification;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import com.jmb.shorturl.model.Url;
import com.jmb.shorturl.exception.ShortUrlException;
import com.jmb.shorturl.exception.UrlException;
import com.jmb.shorturl.exception.UrlNotFoundException;
import com.jmb.shorturl.util.Config;

public class Datasource extends AbstractDatasource {

	private static final Logger log = Logger.getLogger(Datasource.class);

	private static final DatabaseDataSource dsDb = new DatabaseDataSource();
	private static final LoadingCache<Integer, Url> cache;

	static{
		cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(Config.CACHE_TIMEOUT_MINUTE, TimeUnit.MINUTES).build(
				new CacheLoader<Integer, Url>(){
					public Url load(Integer id) throws Exception {
						return dsDb.getUrl(id);
					}
				});
	}

	@Override
	public Url getUrl(int urlId) throws UrlNotFoundException {
		try{
			return cache.get(urlId);
		}catch(ExecutionException e){
			throw new UrlNotFoundException("error while getting url", e);
		}
		
	}

	public Url createNewShortUrl(String longUrl) throws ShortUrlException {
		return dsDb.createNewShortUrl(longUrl);
	}

}