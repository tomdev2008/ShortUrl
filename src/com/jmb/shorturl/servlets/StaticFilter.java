package com.jmb.shorturl.servlets;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;

import java.io.IOException;

import org.apache.log4j.Logger;

public class StaticFilter implements Filter {

  private static final Logger log = Logger.getLogger(StaticFilter.class);

  public static final String SERVLET_PATH = StaticFilter.class.getName() + "/WEB-INF/index.html";
  private String target;

  public void destroy(){
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String path = req.getRequestURI();//.substring(req.getContextPath().length());
    if(path.equals("/") || path.equals("")){
      request.getRequestDispatcher("/WEB-INF/static/index.html").forward(request, response);
    }else{
      request.getRequestDispatcher("/redirects" + path).forward(request, response);
    }
  }

  public void init(FilterConfig config) throws ServletException {
    this.target = config.getInitParameter("target");
  }


}