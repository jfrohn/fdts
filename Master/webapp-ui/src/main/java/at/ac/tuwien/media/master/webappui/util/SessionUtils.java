package at.ac.tuwien.media.master.webappui.util;

import javax.annotation.Nonnull;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

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

    @SuppressWarnings("unchecked")
    public <T> T getManagedBean(@Nonnull final String sName, @Nonnull final Class<T> aClass) {
	final FacesContext aFacesContext = _getFacesContext();

	return (T) aFacesContext.getELContext().getELResolver().getValue(aFacesContext.getELContext(), null, sName);
    }
}