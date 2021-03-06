package com.jmb.shorturl.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import org.apache.log4j.Logger;

import org.apache.commons.validator.UrlValidator;

import com.jmb.shorturl.datasource.AbstractDatasource;
import com.jmb.shorturl.datasource.Datasource;
import com.jmb.shorturl.exception.UrlException;
import com.jmb.shorturl.model.Url;
import com.jmb.shorturl.exception.ShortUrlException;
import com.jmb.shorturl.util.Base62Encoder;
import com.jmb.shorturl.exception.UrlNotFoundException;

@WebServlet("/*")
public class RedirectorServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(RedirectorServlet.class);

	private static final Base62Encoder encoder = new Base62Encoder();

	private static final String[] allowedSchemes = {"http", "https"};
	private static final UrlValidator validator = new UrlValidator();

	private static final int BUFFER_SIZE = 1024;

	private static final String shortRegexString = "^/[a-zA-Z0-9_-]+$";
	private static final Pattern shortUrlPattern = Pattern.compile(shortRegexString);

	private AbstractDatasource ds;

	public RedirectorServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ds = new Datasource();
	}

	private boolean isValidShortUrl(final String url){
		if(url == null){
			return false;
		}
		if(url.length() < 1){
			return false;
		}
		Matcher matcher = shortUrlPattern.matcher(url);
		return matcher.matches();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String requestedPath = request.getRequestURI();
			//re-direct issues with the catch-all link, stuck with this option for now
			if(requestedPath.equals("/")){
				response.setContentType("text/html");
				String indexFileLocation = getServletContext().getRealPath("/") + "/WEB-INF/index.html";
				ServletOutputStream out = response.getOutputStream();
				File indexFile = new File(indexFileLocation);
				log.info("index file => " + indexFile.getAbsolutePath());
				FileInputStream in = new FileInputStream(indexFile);
				byte[] bytes = new byte[BUFFER_SIZE];
				int bytesRead;
				while((bytesRead = in.read(bytes)) != -1){
					out.write(bytes, 0, bytesRead);
				}
				in.close();
				out.close();
				return;
			}

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

	private boolean validLongUrl(String url){
		if(url == null){
			return false;
		}
		if(validator.isValid(url)){
			return true;
		}
		return false;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String requestedLongUrl = request.getParameter("long_url");
		if(!requestedLongUrl.contains("http://")){ //user may not included this on POST
			requestedLongUrl = "http://" + requestedLongUrl;
		}
		if(!validLongUrl(requestedLongUrl)){
			log.error("error, that is an invalid link");
			response.sendError(400);
			return;
		}

		Url createdUrl = null;
		try{
			createdUrl = ds.createNewShortUrl(requestedLongUrl);
		}catch(ShortUrlException e){
			log.error("error creating a new short url", e);
			response.sendError(400);
			return;
		}

		PrintWriter writer = response.getWriter();
		writer.write(createdUrl.toJson());
		writer.flush();
		writer.close();

	}

}