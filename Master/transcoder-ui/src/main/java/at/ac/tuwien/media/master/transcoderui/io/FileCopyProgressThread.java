package at.ac.tuwien.media.master.transcoderui.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.ac.tuwien.media.master.commons.IOnCompleteNotifyListener;
import at.ac.tuwien.media.master.commons.ISetProgress;
import at.ac.tuwien.media.master.commons.ISetText;

public class FileCopyProgressThread extends AbstractNotifierThread {
    private final File m_aInFile;
    private final File m_aOutFile;

    public FileCopyProgressThread(@Nonnull final File aInFile, @Nonnull final File aOutFile, @Nullable final ISetProgress aProgressIndicator,
	    @Nullable final ISetText aProgressText, @Nullable final IOnCompleteNotifyListener aCompleteListener) {
	super(aProgressIndicator, aProgressText, aCompleteListener);

	if (aInFile == null)
	    throw new NullPointerException("in file");
	if (aOutFile == null)
	    throw new NullPointerException("out file");
	if (!aInFile.isFile())
	    throw new IllegalArgumentException("in file does not exist or is not file");

	m_aInFile = aInFile;
	m_aOutFile = aOutFile;
    }

    @SuppressWarnings("resource")
    @Override
    public void run() {

	FileChannel aInChannel = null;
	FileChannel aOutChannel = null;
	try {
	    aInChannel = new FileInputStream(m_aInFile).getChannel();
	    aOutChannel = new FileOutputStream(m_aOutFile).getChannel();

	    // calc chunk count (copy at least 1MB)
	    final long nInFileLenght = m_aInFile.length();
	    int nChunkCount = 1;
	    while ((nInFileLenght / nChunkCount) > 1000000) {
		if (nChunkCount >= 100)
		    break;

		nChunkCount++;
	    }

	    // copy file in chunk sizes
	    final long nChunkSize = nInFileLenght / nChunkCount;
	    long nBytesCopied = nChunkSize;
	    long nBytesLeft = nInFileLenght;
	    double nProgress;
	    for (int i = 0; i < nChunkCount;) {
		aInChannel.transferTo(nBytesCopied, nChunkSize, aOutChannel);

		i++;
		nBytesCopied = i * nChunkSize;
		nBytesLeft -= nChunkSize;
		nProgress = nBytesCopied / (double) nInFileLenght;

		// set values
		_setCallbackValues(nProgress, String.valueOf(nBytesLeft / 1000));
	    }

	    // copy bytes left
	    if (nBytesLeft > 0)
		aInChannel.transferTo(nBytesCopied, nBytesLeft, aOutChannel);

	    // set values
	    _setCallbackValues(100, "0");
	} catch (final Exception aException) {
	    aException.printStackTrace();
	} finally {
	    if (aInChannel != null)
		try {
		    aInChannel.close();
		} catch (final IOException aIOException) {
		}
	    if (aOutChannel != null)
		try {
		    aOutChannel.close();
		} catch (final IOException aIOException) {
		}

	    // notify listener
	    _notifyListener(this);
	}
    }
}