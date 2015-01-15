package at.frohnwieser.mahut.transcoderui.data;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import at.frohnwieser.mahut.ffmpegwrapper.util.FFMPEGUtils;
import at.frohnwieser.mahut.transcoderui.config.Configuration;
import at.frohnwieser.mahut.transcoderui.config.Configuration.EField;
import at.frohnwieser.mahut.transcoderui.util.Utils;
import at.frohnwieser.mahut.webapp.FailedLoginException_Exception;
import at.frohnwieser.mahut.webapp.SetData;
import at.frohnwieser.mahut.wsclient.WSClient;

public class ClientData {
    private static ClientData s_aInstance = new ClientData();
    private Collection<File> m_aMaterials;
    private Collection<File> m_aMetaContentFiles;
    private Collection<SetData> m_aSetDatas;
    private boolean m_bIsRunning;

    private ClientData() {
    }

    public static ClientData getInstance() {
	return s_aInstance;
    }

    public boolean hasMaterials() {
	return CollectionUtils.isNotEmpty(m_aMaterials);
    }

    public boolean hasMetaContentFiles() {
	return CollectionUtils.isNotEmpty(m_aMetaContentFiles);
    }

    public boolean hasSetDatas() {
	return CollectionUtils.isNotEmpty(getSetDatas());
    }

    public boolean isReadyForCopy() {
	return hasMaterials() && getCopyDirectory() != null;
    }

    public boolean isSelectedAndReadyForCopy() {
	return isCopy() && isReadyForCopy();
    }

    public boolean isReadyForUpload() {
	return hasMaterials() && hasSetDatas();
    }

    public boolean isSelectedAndReadyForUpload() {
	return isUpload() && isReadyForUpload();
    }

    public boolean isSelectedAndReadyForUploadAndHasSet() {
	return isSelectedAndReadyForUpload() && getSelectedSetData() != null;
    }

    public boolean isReadyForStart() {
	return isSelectedAndReadyForCopy() || isSelectedAndReadyForUploadAndHasSet();
    }

    // only getters and setters
    @Nonnull
    public String getUsername() {
	return Configuration.getInstance().getAsStringOrEmpty(EField.USERNAME);
    }

    public boolean setUsername(@Nonnull final String sUsername) {
	if (StringUtils.isNotEmpty(sUsername)) {
	    Configuration.getInstance().set(EField.USERNAME, sUsername);

	    return true;
	}

	return false;
    }

    @Nonnull
    public String getPassword() {
	return Configuration.getInstance().getDecryptedAsStringOrEmpty(EField.PASSWORD);
    }

    public boolean setPassword(@Nonnull final String sPassword) {
	if (StringUtils.isNotEmpty(sPassword)) {
	    Configuration.getInstance().setEncrypted(EField.PASSWORD, sPassword);

	    return true;
	}

	return false;
    }

    @Nullable
    public URL getServerURL() throws MalformedURLException {
	return new URL(Configuration.getInstance().getAsStringOrEmpty(EField.SERVER_URL));
    }

    public boolean setServerURL(@Nonnull final URL aServerURL) {
	Configuration.getInstance().set(EField.SERVER_URL, aServerURL.toString());

	return true;
    }

    public boolean setServerURL(@Nonnull final String sServerURL) throws MalformedURLException {
	if (StringUtils.isNotEmpty(sServerURL))
	    return setServerURL(new URL(sServerURL));

	return false;
    }

    @Nonnull
    public Locale getLocale() {
	return new Locale(Configuration.getInstance().getAsStringOrEmpty(EField.LOCALE));
    }

    public boolean setLocale(@Nullable final Locale aLocale) {
	if (aLocale != null) {
	    Configuration.getInstance().set(EField.LOCALE, aLocale.getLanguage());

	    return true;
	}

	return false;
    }

    @Nonnull
    public Collection<File> getMaterials() {
	if (m_aMaterials == null)
	    m_aMaterials = new TreeSet<File>();

	return m_aMaterials;
    }

    public boolean addMaterials(@Nullable final List<File> aInFileList) {
	if (CollectionUtils.isNotEmpty(aInFileList)) {
	    // add only supported files once
	    final List<File> aNewVideoFileList = new ArrayList<File>();
	    for (final File aUploadFile : aInFileList)
		if (FFMPEGUtils.isFormatSupportedForDecoding(FilenameUtils.getExtension(aUploadFile.getName())))
		    aNewVideoFileList.add(aUploadFile);
		else
		    ; // TODO: WARNING?

	    if (CollectionUtils.isNotEmpty(aNewVideoFileList)) {
		getMaterials().addAll(aNewVideoFileList);

		// save last shown upload folder
		Configuration.getInstance().set(EField.FILEPATH_MATERIALS,
		        aNewVideoFileList.get(aNewVideoFileList.size() - 1).getParentFile().getAbsolutePath());

		return true;
	    }
	}

	return false;
    }

    @Nullable
    public File getMaterialsDirectory() {
	return Utils.getDirectoryOrNull(Configuration.getInstance().getAsString(EField.FILEPATH_MATERIALS));
    }

    @Nonnull
    public Collection<File> getMetaContentFiles() {
	if (m_aMetaContentFiles == null)
	    m_aMetaContentFiles = new TreeSet<File>();

	return m_aMetaContentFiles;
    }

    public boolean addMetaContentFiles(@Nullable final List<File> aMetaContentFiles) {
	if (CollectionUtils.isNotEmpty(aMetaContentFiles)) {
	    getMetaContentFiles().addAll(aMetaContentFiles);

	    // save last shown metadata folder
	    Configuration.getInstance().set(EField.FILEPATH_METACONTENT, aMetaContentFiles.get(aMetaContentFiles.size() - 1).getParentFile().getAbsolutePath());

	    return true;
	}

	return false;
    }

    @Nullable
    public File getMetaContentFilesDirectory() {
	return Utils.getDirectoryOrNull(Configuration.getInstance().getAsString(EField.FILEPATH_METACONTENT));
    }

    @Nullable
    public File getCopyDirectory() {
	return Utils.getDirectoryOrNull(Configuration.getInstance().getAsString(EField.FILEPATH_COPY));
    }

    public boolean setCopyDirectory(@Nullable final File aCopyFile) {
	if (aCopyFile != null && aCopyFile.isDirectory()) {
	    Configuration.getInstance().set(EField.FILEPATH_COPY, aCopyFile.getAbsolutePath());

	    return true;
	}

	return false;
    }

    public boolean isCopy() {
	return Configuration.getInstance().getAsBoolean(EField.IS_SELECTED_COPY);
    }

    public void setIsCopy(final boolean bIsCopy) {
	Configuration.getInstance().set(EField.IS_SELECTED_COPY, bIsCopy);
    }

    private void _setUpWSClient() throws MalformedURLException {
	final String sUsername = getUsername();
	final String sPassword = getPassword();
	final URL aServerURL = getServerURL();

	if (StringUtils.isNotEmpty(sUsername) && StringUtils.isNotEmpty(sPassword) && aServerURL != null) {
	    WSClient.getInstance().setUsername(sUsername);
	    WSClient.getInstance().setPassword(sPassword);
	    WSClient.getInstance().setWSURL(aServerURL);

	    WSClient.getInstance().createEndpoint();
	}
    }

    @Nonnull
    public void reloadWSData() throws MalformedURLException, FailedLoginException_Exception {
	m_aSetDatas = new ArrayList<SetData>();
	_setUpWSClient();

	m_aSetDatas = WSClient.getInstance().getSets();

	// also set selected project if possible
	if (CollectionUtils.isNotEmpty(m_aSetDatas)) {
	    if (m_aSetDatas.size() == 1)
		setSelectedSet(m_aSetDatas.iterator().next().getId());
	} else
	    ; // TODO: WARNING?
    }

    public Collection<SetData> getSetDatas() {
	try {
	    if (m_aSetDatas == null)
		reloadWSData();
	} catch (MalformedURLException | FailedLoginException_Exception aException) {
	    throw new RuntimeException(aException);
	}

	return m_aSetDatas;
    }

    @Nullable
    public SetData getSelectedSetData() {
	// check if selected project is contained in project list
	final long sSelectedSetId = Configuration.getInstance().getAsLong(EField.SELECTED_SET);

	return getSetDatas().stream().filter(aSetData -> aSetData.getId() == sSelectedSetId).findFirst().orElse(null);
    }

    public void setSelectedSet(@Nullable final long nSetId) {
	Configuration.getInstance().set(EField.SELECTED_SET, nSetId);
    }

    public boolean isUpload() {
	return Configuration.getInstance().getAsBoolean(EField.IS_SELECTED_UPLOAD);
    }

    public void setIsUpload(final boolean bIsUpload) {
	Configuration.getInstance().set(EField.IS_SELECTED_UPLOAD, bIsUpload);
    }

    public boolean isRunning() {
	return m_bIsRunning;
    }

    public void setRunning(final boolean isRunning) {
	m_bIsRunning = isRunning;
    }
}