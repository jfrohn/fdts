package at.ac.tuwien.media.master.webappui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import at.ac.tuwien.media.master.webappui.beans.Credentials;
import at.ac.tuwien.media.master.webappui.controller.LoginController;
import at.ac.tuwien.media.master.webappui.util.EPage;
import at.ac.tuwien.media.master.webappui.util.Value;

public class PageFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest aServletRequest, final ServletResponse aServletResponse, final FilterChain aFilterChain) throws IOException,
	    ServletException {
	final HttpServletRequest aRequest = (HttpServletRequest) aServletRequest;
	final HttpServletResponse aResponse = (HttpServletResponse) aServletResponse;

	final String sContextPath = aRequest.getContextPath();
	final String sRequestSitePath = StringUtils.removeStart(aRequest.getRequestURI(), sContextPath);

	// do not filter resources
	if (!(sRequestSitePath.startsWith(Value.FOLDER_JAVAX) || sRequestSitePath.startsWith(Value.FOLDER_RESOURCES) || sRequestSitePath
	        .equals(Value.RES_NOT_FOUND))) {

	    EPage aRequestPage = EPage.getFromName(sRequestSitePath);
	    EPage aRedirectPage = null;

	    // got rewritten url - check credentials
	    if (aRequestPage != null) {
		final LoginController aLoginController = (LoginController) aRequest.getSession().getAttribute(Value.CONTROLLER_LOGIN);
		final boolean bIsLoggedIn = aLoginController != null && aLoginController.isLoggedIn();

		// login page
		if (aRequestPage.equals(EPage.ROOT) || aRequestPage.equals(EPage.LOGIN)) {
		    // if logged in redirect to start page
		    if (bIsLoggedIn)
			aRedirectPage = EPage.START;
		}
		// check credentials for every available page
		else if (bIsLoggedIn) {
		    final Credentials aCredentials = (Credentials) aRequest.getSession().getAttribute(Value.BEAN_CREDENTIALS);

		    // check credentials for page
		    if (!aCredentials.getRole().is(aRequestPage.getRole()))
			aRedirectPage = EPage.START;
		}
		// if not logged in redirect to login page
		else
		    aRedirectPage = EPage.LOGIN;

		// send redirect
		if (aRedirectPage != null)
		    aResponse.sendRedirect(sContextPath + aRedirectPage.getPath());
		// or "silently" forward to real page
		else
		    aRequest.getRequestDispatcher(aRequestPage.getPath()).forward(aRequest, aResponse);
	    }
	    // try to rewrite direct urls
	    else {
		aRequestPage = EPage.getFromPath(sRequestSitePath);

		// got valid direct url
		if (aRequestPage != null) {
		    // exclude redirects to current site... for navigation
		    final String sReferrer = aRequest.getHeader("Referer");
		    if (StringUtils.isEmpty(sReferrer) || !sReferrer.endsWith(aRequestPage.getName())) {
			aResponse.setStatus(301);
			aResponse.sendRedirect(sContextPath + "/" + aRequestPage.getName());
		    } else
			// continue filter chain
			aFilterChain.doFilter(aRequest, aResponse);
		}
		// page does not exist - send redirect to root
		else
		    aResponse.sendRedirect(sContextPath + EPage.ROOT.getPath());
	    }
	} else
	    // continue filter chain
	    aFilterChain.doFilter(aRequest, aResponse);
    }

    @Override
    public void destroy() {
	// this method intentionally left blank!
    }

    @Override
    public void init(final FilterConfig arg0) throws ServletException {
	// this method intentionally left blank!
    }
}
