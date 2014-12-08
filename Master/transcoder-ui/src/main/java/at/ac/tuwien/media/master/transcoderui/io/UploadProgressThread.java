package at.ac.tuwien.media.master.transcoderui.io;

import at.ac.tuwien.media.master.webapp.AssetData;
import at.ac.tuwien.media.master.wsclient.WSClient;

public class UploadProgressThread extends AbstractNotifierThread {
    private final long f_nSetId;

    public UploadProgressThread(final long nSetId) {
	f_nSetId = nSetId;
    }

    @Override
    protected void _processQueue() {
	try {
	    Object aObject = null;

	    // upload assets
	    if (WSClient.getInstance().isCreated()) {
		while (((aObject = _takeFromQueue()) instanceof AssetData) && !m_bTerminate)
		    if (aObject != null) {
			final AssetData aAssetData = (AssetData) aObject;

			_setCallbackValues(-1, aAssetData.getName(), "");

			if (WSClient.getInstance().uploadAsset(f_nSetId, aAssetData))
			    _setCallbackValues(1, aAssetData.getName(), "");
			else
			    throw new RuntimeException("cannot send asset");
		    }

		// notify listener
		_setCallbackValues(1, "", "");
		_notifyOnComplete(this);
	    } else
		throw new IllegalStateException("ws end point not yet ready");
	} catch (final Exception aException) {
	    throw new RuntimeException(aException);
	}
    }
}
