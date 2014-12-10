package at.ac.tuwien.media.master.ffmpegwrapper;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import at.ac.tuwien.media.master.ffmpegwrapper.util.FFMPEGCall;
import at.ac.tuwien.media.master.ffmpegwrapper.util.FFMPEGUtils;
import at.ac.tuwien.media.master.ffmpegwrapper.util.FFPROBECall;

public final class FFMPEGWrapper {
    public enum EFormat {
	OGG("ogg",
	        "libvorbis",
	        "libtheora"),
	MP4("mp4",
	        "libfaac",
	        "libx264"),
	WEBM("webm",
	        "libvorbis",
	        "libvpx");

	private final String f_sName;
	private final String f_sACodec;
	private final String f_sVCodec;

	private EFormat(@Nonnull final String sName, @Nonnull final String sACodec, @Nonnull final String sVCodec) {
	    f_sName = sName;
	    f_sACodec = sACodec;
	    f_sVCodec = sVCodec;
	}

	public String getName() {
	    return f_sName;
	}

	public String getACodec() {
	    return f_sACodec;
	}

	public String getVCodec() {
	    return f_sVCodec;
	}
    }

    public enum EQuality {
	P240("scale=240:-1",
	        "400k"),
	P320("scale=320:-1",
	        "600k"),
	P360("scale=360:-1",
	        "750k"),
	P480("scale=480:-1",
	        "1000k"),
	P720("scale=720:-1",
	        "2500k"),
	P1080("scale=1080:-1",
	        "4500k");

	private final String f_sScale;
	private final String f_sBitrate;

	private EQuality(@Nonnull final String sScale, @Nonnull final String sBitrate) {
	    f_sScale = sScale;
	    f_sBitrate = sBitrate;
	}

	public String getScale() {
	    return f_sScale;
	}

	public String getBitrate() {
	    return f_sBitrate;
	}
    }

    private FFMPEGWrapper() {
    }

    public static File getOutputFile(@Nonnull final File aInFile, @Nonnull final File aOutDirectory, @Nonnull final String sFileExtenstion) {
	if (aInFile == null || !aInFile.isFile())
	    throw new NullPointerException("input file");
	if (aOutDirectory == null || !aOutDirectory.isDirectory())
	    throw new NullPointerException("output directory");
	if (StringUtils.isEmpty(sFileExtenstion))
	    throw new IllegalArgumentException("file extenstion");

	return new File(aOutDirectory.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(aInFile.getName()) + "." + sFileExtenstion);
    }

    public static File getOutputFile(@Nonnull final File aInFile, @Nonnull final File aOutDirectory, @Nonnull final EFormat aFormat) {
	if (aFormat == null)
	    throw new NullPointerException("format");

	return getOutputFile(aInFile, aOutDirectory, aFormat.getName());
    }

    @Nonnull
    public static JSONObject metadata(@Nonnull final File aInFile) {
	Scanner aScanner = null;

	try {
	    if (aInFile == null || !aInFile.isFile())
		throw new NullPointerException("input file");

	    // ffprobe -v quiet -print_format json -show_format -show_streams
	    final Process aProcess = FFPROBECall.execute("-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", aInFile.getAbsolutePath());
	    aScanner = new Scanner(aProcess.getInputStream());

	    // parse output
	    final StringBuilder aSB = new StringBuilder(1024);
	    while (aScanner.hasNextLine())
		aSB.append(aScanner.nextLine().trim());

	    return new JSONParser(JSONParser.MODE_PERMISSIVE).parse(aSB.toString(), JSONObject.class);
	} catch (final ParseException | IOException aException) {
	    throw new RuntimeException(aException);
	} finally {
	    if (aScanner != null)
		aScanner.close();
	}
    }

    @Nonnull
    public static JSONObject metadata(@Nonnull final String sInputVideo) {
	if (StringUtils.isEmpty(sInputVideo))
	    throw new IllegalArgumentException("input video");

	return metadata(new File(sInputVideo));
    }

    public static boolean thumbnail(@Nonnull final File aInFile, @Nonnull final File aOutDirectory, @Nonnull final String sFileType,
	    final @Nonnull EQuality aQuality, @Nonnull final String sTime) {
	if (aInFile == null || !aInFile.isFile())
	    throw new NullPointerException("input file");
	if (aOutDirectory == null || !aOutDirectory.isDirectory())
	    throw new NullPointerException("output directory");
	if (StringUtils.isEmpty(sFileType))
	    throw new IllegalArgumentException("sFileType");
	if (aQuality == null)
	    throw new NullPointerException("quality");
	if (StringUtils.isEmpty(sTime))
	    throw new IllegalArgumentException("time");

	try {
	    final File aOutFile = getOutputFile(aInFile, aOutDirectory, sFileType);
	    // ffmpeg -i input.mxf -ss 00:00:00.010 -f image2 -vframes 1
	    // -vf scale=320:-1 out.jpg
	    final Process aProcess = FFMPEGCall.execute("-i", aInFile.getAbsolutePath(), "-ss", sTime, "-f", "image2", "-vframes", "1", "-vf",
		    aQuality.getScale(), aOutFile.getAbsolutePath());

	    while (aProcess.isAlive())
		;

	    return aProcess.exitValue() == 0;
	} catch (final IOException aIOException) {
	    throw new RuntimeException(aIOException);
	}
    }

    @Nullable
    public static boolean thumbnail(@Nonnull final String sInputVideo, @Nonnull final String sOutDirectory, @Nonnull final String sFileType,
	    final @Nonnull EQuality aQuality, final String sTime) {
	if (StringUtils.isEmpty(sInputVideo))
	    throw new IllegalArgumentException("input video");
	if (StringUtils.isEmpty(sOutDirectory))
	    throw new IllegalArgumentException("output directory");

	return thumbnail(new File(sInputVideo), new File(sOutDirectory), sFileType, aQuality, sTime);
    }

    @Nullable
    public static Process transcode(@Nonnull final File aInFile, @Nonnull final File aOutDirectory, final @Nonnull EFormat aFormat,
	    final @Nonnull EQuality aQuality) {
	if (aInFile == null || !aInFile.isFile())
	    throw new NullPointerException("input file");
	if (aOutDirectory == null || !aOutDirectory.isDirectory())
	    throw new NullPointerException("output directory");
	if (aFormat == null)
	    throw new NullPointerException("format");
	if (aQuality == null)
	    throw new NullPointerException("quality");
	if (!FFMPEGUtils.isFormatSupportedForDecoding(FilenameUtils.getExtension(aInFile.getName())))
	    throw new IllegalArgumentException("input file format");

	try {
	    final File aOutFile = getOutputFile(aInFile, aOutDirectory, aFormat);
	    // ffmpeg -y -i in.mxf -c:a libfaac -c:v libx264 -b:v 4500k -strict
	    // -2 output.mp4
	    return FFMPEGCall.execute("-y", "-i", aInFile.getAbsolutePath(), "-c:a", aFormat.getACodec(), "-c:v", aFormat.getVCodec(), "-b:v",
		    aQuality.getBitrate(), "-vf", aQuality.getScale(), "-strict", "-2", aOutFile.getAbsolutePath());
	} catch (final IOException aIOException) {
	    throw new RuntimeException(aIOException);
	}
    }

    @Nullable
    public static Process transcode(@Nonnull final String sInputVideo, @Nonnull final String sOutDirectory, final @Nonnull EFormat aFormat,
	    final @Nonnull EQuality aQuality) {
	if (StringUtils.isEmpty(sInputVideo))
	    throw new IllegalArgumentException("input video");
	if (StringUtils.isEmpty(sOutDirectory))
	    throw new IllegalArgumentException("output directory");

	return transcode(new File(sInputVideo), new File(sOutDirectory), aFormat, aQuality);
    }
}
