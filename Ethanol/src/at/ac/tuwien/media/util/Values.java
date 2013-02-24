package at.ac.tuwien.media.util;

import java.io.File;

import android.graphics.Color;
import android.os.Environment;
import at.ac.tuwien.media.io.file.EImageSize;

public final class Values {
	// ALL OTHERS
	public static final boolean RESET = false;
	public static final String JPG = ".jpg";
	public enum EProgram {PROG_1, PROG_2;}
	
	// FOLDERS
	public static final String SDCARD = Environment.getExternalStorageDirectory().getPath() + File.separator;
	public static final String REGEX_IMAGE_DIRECTORIES = "[0-9]{4}";
	private static final String RESIZED_IMAGE_FOLDER = "preview";
	public static final String THUMBNAIL_FOLDER_A = RESIZED_IMAGE_FOLDER + EImageSize.A.getName();
	public static final String THUMBNAIL_FOLDER_B = RESIZED_IMAGE_FOLDER + EImageSize.B.getName();
	public static final String THUMBNAIL_FOLDER_C = RESIZED_IMAGE_FOLDER + EImageSize.C.getName();
	public static final String THUMBNAIL_FOLDER_D = RESIZED_IMAGE_FOLDER + EImageSize.D.getName();
	public static final String THUMBNAIL_FOLDER_E = RESIZED_IMAGE_FOLDER + EImageSize.E.getName();
	public static final String THUMBNAIL_FOLDER_F = RESIZED_IMAGE_FOLDER + EImageSize.F.getName();
	public static final String THUMBNAIL_FOLDER_G = RESIZED_IMAGE_FOLDER + EImageSize.G.getName();
	public static final String THUMBNAIL_FOLDER_H = RESIZED_IMAGE_FOLDER + EImageSize.H.getName();
	public static final String THUMBNAIL_FOLDER_I = RESIZED_IMAGE_FOLDER + EImageSize.I.getName();
	
	// IMAGES
	public static final String STATUS_FILE_NAME = ".c2h6o";
	public static final String FIRST_IMAGE_NAME = "0001" + JPG;
	public static final String DEFAULT_IMAGE = SDCARD + "images/default" + JPG; //TODO set me!
	public static final int IMAGE_COMPRESS_QUALITY = 50;
	public static final int IMAGE_PADDING = 3;
	public static final int IMAGE_BACKGROUND_COLOR = Color.TRANSPARENT;
	public static final int IMAGE_HIGHLIGHT_WIDTH = 25;
	
	// GESTURES
	public static final int SWIPE_THRESHOLD_VELOCITY = 50;
	public static final int SWIPE_INTERVAL_FAST = 6;
	public static final int SWIPE_INTERVAL_HALF = 3;
	public enum EDirection {PREVIOUS, NEXT;}
	
	// VIEW COORDINATES
	public static final int HORIZONTAL_TOP = 0;
	public static final int HORIZONTAL_TOP_ROW_EDGE = 26;
	public static final int HORIZONTAL_MAIN_SECTION_EDGE = 74;
	public static final int HORIZONTAL_BOTTOM = 100;
	
	public static final int VERTICAL_LEFT = 0;
	public static final int VERTICAL_LEFT_10_PERCENT = 10;
	public static final int VERTICAL_LEFT_90_PERCENT = 90;
	public static final int VERTICAL_RIGHT = 100;
	
	public static final int VERTICAL_FIRST_PICTURE_EDGE = 28;
	public static final int VERTICAL_SECOND_PICTURE_EDGE = 72;
}