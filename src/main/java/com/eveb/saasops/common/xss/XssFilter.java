package com.eveb.saasops.common.xss;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * XSS过滤
 */
@Component
public class XssFilter implements Filter {

	@Override
	public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
				(HttpServletRequest) request);
		chain.doFilter(xssRequest, response);
	}
	@Override
	public void destroy() {
	}

}