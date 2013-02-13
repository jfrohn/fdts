package at.ac.tuwien.media.util;

import android.os.Environment;

public final class Values {
	// ALL OTHERS
	public static final String JPG = ".jpg";
	public static final int IMAGE_COMPRESS_QUALITY = 100;
	
	// FOLDERS
	public static final String SDCARD = Environment.getExternalStorageDirectory().getPath() + "/";
	public static final String REGEX_IMAGE_DIRECTORIES = "[0-9]{4}";
	private static final String RESIZED_IMAGE_FOLDER = "preview";
	public static final String THUMBNAIL_FOLDER_A = RESIZED_IMAGE_FOLDER + EImageSize.A.getName();
	public static final String THUMBNAIL_FOLDER_B = RESIZED_IMAGE_FOLDER + EImageSize.B.getName();
	public static final String THUMBNAIL_FOLDER_C = RESIZED_IMAGE_FOLDER + EImageSize.C.getName();
	public static final String THUMBNAIL_FOLDER_D = RESIZED_IMAGE_FOLDER + EImageSize.D.getName();
	public static final String THUMBNAIL_FOLDER_E = RESIZED_IMAGE_FOLDER + EImageSize.E.getName();
	public static final String THUMBNAIL_FOLDER_F = RESIZED_IMAGE_FOLDER + EImageSize.F.getName();
	
	// IMAGES
	public static final String STATUS_FILE_NAME = ".c2h6o";
	public static final String FIRST_IMAGE_NAME = "0001" + JPG;
	public static final String DEFAULT_IMAGE = SDCARD + "images/default" + JPG; //TODO set me!
	
	// GESTURE
	public static final String SWIPE_LEFT = "left_swipe";
	public static final String SWIPE_RIGHT = "right_swipe";
	public static final String SWIPE_UP = "up_swipe";
	public static final String SWIPE_DOWN = "down_swipe";
	public static final int FAST_SWIPE_INTERVAL = 5;
	
}