package at.ac.tuwien.media.master.webapp.util;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import at.ac.tuwien.media.master.webappui.page.EPage;

public class SessionUtils {
    private static SessionUtils s_aInstance = new SessionUtils();

    private SessionUtils() {
    }

    public static SessionUtils getInstance() {
	return s_aInstance;
    }

    private static FacesContext _getFacesContext() {
	return FacesContext.getCurrentInstance();
    }

    private static ExternalContext _getExternalContext() {
	return _getFacesContext().getExternalContext();
    }

    public void invalidateSession() {
	final HttpSession aSession = (HttpSession) _getExternalContext().getSession(true);
	if (aSession != null)
	    aSession.invalidate();
    }

    public void destroyManagedBean(@Nonnull final String sName) {
	_getExternalContext().getSessionMap().put(sName, null);
    }

    @Nullable
    public EPage getCurrentPage() {
	final String sCurrentViewId = _getFacesContext().getViewRoot().getViewId();

	return EPage.getFromPath(sCurrentViewId);
    }

    public String getRequestParameter(final String sRequestParameter) {
	final Map<String, String> aRequestParameterMap = _getExternalContext().getRequestParameterMap();

	return aRequestParameterMap.get(sRequestParameter);
    }

    @SuppressWarnings("unchecked")
    public <T> T getManagedBean(@Nonnull final String sName, @Nonnull final Class<T> aClass) {
	final FacesContext aFacesContext = _getFacesContext();

	return (T) aFacesContext.getELContext().getELResolver().getValue(aFacesContext.getELContext(), null, sName);
    }
}