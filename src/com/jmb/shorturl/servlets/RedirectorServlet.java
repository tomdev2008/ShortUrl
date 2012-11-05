package com.jmb.shorturl.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jmb.shorturl.datasource.AbstractDatasource;
import com.jmb.shorturl.datasource.Datasource;
import com.jmb.shorturl.exception.UrlException;
import com.jmb.shorturl.model.Url;
import com.jmb.shorturl.util.Base62Encoder;
import com.jmb.shorturl.exception.UrlNotFoundException;

@WebServlet("/*")
public class RedirectorServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(RedirectorServlet.class);

	private static final Base62Encoder encoder = new Base62Encoder();

	private static final String shortRegexString = "^/[a-zA-Z0-9_-]+$";
	private static final Pattern shortUrlPattern = Pattern.compile(shortRegexString);

	private AbstractDatasource ds;

	public RedirectorServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		ds = new Datasource();
	}

	private boolean isValidShortUrl(final String url){
		if(url == null){
			return false;
		}

		Matcher matcher = shortUrlPattern.matcher(url);
		return matcher.matches();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String requestedPath = request.getRequestURI();
			if(!isValidShortUrl(requestedPath)){
				log.info("invalid short url requested url=" + requestedPath);
				response.sendError(404);
				return;
			}
			String[] shortPartArray = requestedPath.split("/");
			String shortPart = shortPartArray[shortPartArray.length - 1];
			Url foundUrl = ds.getUrl(shortPart);
			log.info("short url found " + foundUrl.toString());
			if (foundUrl != null) {
				response.sendRedirect(foundUrl.getLongUrlString());
				return;
			} else {
				response.sendError(404);
				return;
			}

		} catch (UrlNotFoundException e) {
			response.sendError(404);
			return;
		}
	}

}