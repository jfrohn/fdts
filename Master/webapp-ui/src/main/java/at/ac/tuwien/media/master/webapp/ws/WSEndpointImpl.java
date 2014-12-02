package at.ac.tuwien.media.master.webapp.ws;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.security.auth.login.FailedLoginException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOM;

import at.ac.tuwien.media.master.webapp.ws.data.AssetData;
import at.ac.tuwien.media.master.webapp.ws.data.SetData;
import at.ac.tuwien.media.master.webappapi.db.manager.impl.MetaManager;
import at.ac.tuwien.media.master.webappapi.db.manager.impl.SetManager;
import at.ac.tuwien.media.master.webappapi.db.manager.impl.UserManager;
import at.ac.tuwien.media.master.webappapi.db.model.Set;
import at.ac.tuwien.media.master.webappapi.db.model.User;

@MTOM
@WebService(endpointInterface = "at.ac.tuwien.media.master.webapp.ws.IWSEndpoint")
public class WSEndpointImpl implements IWSEndpoint {
    String HEADER_USERNAME = "username";
    String HEADER_PASSWORD = "password";

    @Resource
    WebServiceContext aWSContext;

    @Nullable
    private User _authenticate() throws FailedLoginException {
	final Map<?, ?> aHeaders = (Map<?, ?>) aWSContext.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS);
	final List<?> aUsers = (List<?>) aHeaders.get(HEADER_USERNAME);
	final List<?> aPasswords = (List<?>) aHeaders.get(HEADER_PASSWORD);

	if (aUsers != null && aUsers.size() == 1 && aPasswords != null && aPasswords.size() == 1) {
	    final String sUsername = aUsers.get(0).toString();
	    final String sPassword = aPasswords.get(0).toString();

	    return UserManager.getInstance().get(sUsername, sPassword);
	}

	throw new FailedLoginException("wrong username or password");
    }

    @Override
    public boolean uploadAsset(final long nParentSetId, @Nonnull final AssetData aAssetData) throws FailedLoginException {
	if (aAssetData != null) {
	    final User aUser = _authenticate();
	    if (aUser != null) {
		System.out.println("UPLOAD " + aAssetData.getId() + " " + aAssetData.getName() + "\t" + aAssetData.getMetaContent() + "\t"
		        + aAssetData.isMetaContent() + "\tfor " + nParentSetId);

		return true;
	    }
	}

	return false;
    }

    @Override
    public boolean createSet(final long nParentSetId, @Nonnull final SetData aSetData) throws FailedLoginException {
	if (aSetData != null) {
	    final User aUser = _authenticate();
	    if (aUser != null) {
		System.out.println("CREATE " + aSetData.getId() + " " + aSetData.getName() + "\tfor " + nParentSetId);

		SetManager.getInstance().save(nParentSetId, new Set(aSetData.getId(), aSetData.getName(), aSetData.getMetaContent()));
		// TODO move

		return true;
	    }
	}

	return false;
    }

    @Override
    @Nonnull
    public SetData[] getAllSets() throws FailedLoginException {
	final User aUser = _authenticate();

	if (aUser != null) {
	    System.out.println("GET PROJECTS");

	    final Collection<Set> aSets = MetaManager.getInstance().allSetsForUser(aUser);

	    return aSets.stream().map(aSet -> new SetData(aSet)).toArray(SetData[]::new);
	}

	return new SetData[] {};
    }
}