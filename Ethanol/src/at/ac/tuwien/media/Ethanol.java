package at.ac.tuwien.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import at.ac.tuwien.media.io.file.EThumbnailType;
import at.ac.tuwien.media.io.file.FileIO;
import at.ac.tuwien.media.io.gesture.ERectangleType;
import at.ac.tuwien.media.io.gesture.EthanolGestureDetector;
import at.ac.tuwien.media.util.Values;
import at.ac.tuwien.media.util.Values.EDirection;
import at.ac.tuwien.media.util.Values.EProgram;
import at.ac.tuwien.media.util.exception.EthanolException;

public class Ethanol extends Activity implements IImageSwipe {
	private final static String VIDEO_NAME = "images";
	private GestureDetector gestureDetector;

	private FileIO io;
	private int currentThumbnailNo = -1;
	private File fixedThumbnail = null;

	private ImageView[] imageViews;
	
	private List<File> thumbnailFiles;
	private List<Bitmap> thumbnailsD;
	private List<Bitmap> thumbnailsE;
	private List<Bitmap> thumbnailsF;
	private List<Bitmap> thumbnailsG;
	private List<Bitmap> thumbnailsH;
	private List<Bitmap> thumbnailsI;
	
	private List<EThumbnailType> thumbnailSizesTopRow;
	private List<EThumbnailType> thumbnailSizesBottomRow;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
		setContentView(R.layout.activity_ethanol);
		
		// load thumbnails
        loadThumbnails();
        
        // Gesture detection
        initGestureDetection();
        
        // add view items
        initViews();
        
        // load first image
        skipToImage(EDirection.FORWARD, 1);
        
		} catch (EthanolException ee) {
			makeToast("Cannot start Ethanol: " + ee.getMessage());
			finish();
		} catch (Exception e) {
			makeToast("Cannot start Ethanol");
			finish();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	private void loadThumbnails() throws EthanolException {
		// load images from sdCard
		// create thumbnails if needed
		io = new FileIO();
		thumbnailFiles = io.loadThumbnails(VIDEO_NAME);

		// since they are the biggest files, thumbnail sizes A - C are loaded directly,
		// for performance issues thumbnail sizes D - I are cached
		thumbnailsD = io.getThumbnailList(EThumbnailType.D);
		thumbnailsE = io.getThumbnailList(EThumbnailType.E);
		thumbnailsF = io.getThumbnailList(EThumbnailType.F);
		thumbnailsG = io.getThumbnailList(EThumbnailType.G);
		thumbnailsH = io.getThumbnailList(EThumbnailType.H);
		thumbnailsI = io.getThumbnailList(EThumbnailType.I);
	}
	
	private void initGestureDetection() {
		// get the display size
		Display display = getWindowManager().getDefaultDisplay();
		Point displaySize = new Point();
		display.getSize(displaySize);
		
		// init new gesture detector
		gestureDetector = new GestureDetector(this, new EthanolGestureDetector(this, displaySize));
	}
	
	private void initViews() {
		// create an imageView for every thumbnail:
    	imageViews = new ImageView[thumbnailFiles.size()];
    	
    	for (int i = 0; i < imageViews.length; i++) {
    		imageViews[i] = addViews(i);
    	}
	}
	
	private ImageView addViews(int viewId) {
		// create a new image view with the given id
		ImageView iv = new ImageView(this);
		iv.setId(viewId);
		
		return iv;
	}
	
	@Override
	public void skipToImage(EDirection direction, int interval) {
		// if we have a fixed image disable fast swipe,
		// i.e. make a only short swipe
		if (Math.abs(interval) == 2 && fixedThumbnail != null) {
			interval *= 0.5;
		}
		
		switch (direction) {
			case PREVIOUS:
				// skip to a previous picture with the given interval
				currentThumbnailNo -= interval;
				break;
			case FORWARD:
				// skip to a next picture with the given interval
				currentThumbnailNo += interval;
				break;
			default:
				// do nothing
				return;
		}
		
		// update the screen
		updateImageViews();
	}
	
	@Override
	public void skipToImageFromRow(int row, int percent) {
		int thumbsInRow;
		
		// which row to calculate from?
		switch (row) {
			case Values.HORIZONTAL_TOP:
				// we have ... thumbnails in the top row
				thumbsInRow = currentThumbnailNo - 2;
				// determine the current thumbnail number
				currentThumbnailNo = Math.round((thumbsInRow * percent) / 100);
				break;
			case Values.HORIZONTAL_BOTTOM:
				// we have ... thumbnails in the bottom row
				thumbsInRow = thumbnailFiles.size() - currentThumbnailNo - 2;
				// determine the current thumbnail number
				currentThumbnailNo = currentThumbnailNo + 2 + Math.round((thumbsInRow * percent) / 100);
				break;
			default:
				// do nothing
				return;
		}
		
		// update the screen
		updateImageViews();
	}
		
	public void updateImageViews() {		
		// check image boundaries
		if (currentThumbnailNo < 0) {
			currentThumbnailNo = 0;
		} else if (currentThumbnailNo > (imageViews.length - 1)) {
			currentThumbnailNo = (imageViews.length - 1);
		}
		
		// first remove all image views
		((LinearLayout) findViewById(R.id.row_top)).removeAllViews();
		((LinearLayout) findViewById(R.id.main_section)).removeAllViews();
		((LinearLayout) findViewById(R.id.row_bottom)).removeAllViews();
		
		// now reset the image views with the right thumbnails
		// some configuration values
		int layoutId;
		EThumbnailType thumbnailType;
		int offsetToBottomRow = (fixedThumbnail == null) ? 2 : 1;
		boolean passedFixedImage = false;
		
		thumbnailSizesTopRow = calculateThumbnailSizes(Math.max((currentThumbnailNo - 1), 0), true);
		thumbnailSizesBottomRow = calculateThumbnailSizes((thumbnailFiles.size() - (currentThumbnailNo + 1)), false);

		// get the right parameters and set the thumbnails
		for (int i = 0; i < thumbnailFiles.size(); i++) {
			
			// 1) already view thumbnails in the top row
			if (i < (currentThumbnailNo - 1)) {
				layoutId = R.id.row_top;
				thumbnailType = EThumbnailType.I;
				thumbnailType = thumbnailSizesTopRow.get(i);
				
			// 2) there are three (or at least two) thumbnails in the main section
			} else if (i < (currentThumbnailNo + offsetToBottomRow)) {
				layoutId = R.id.main_section;
				
				// the current thumbnail is in the center
				if (i == currentThumbnailNo) {
					// there is no fixed thumbnail -> display the current one in the center
					if (fixedThumbnail == null) {
						thumbnailType = EThumbnailType.A;
						
					// display the fixed thumbnail in the center and the successor on the right
					} else {
						thumbnailType = EThumbnailType.B;
						
						addImageViewToLayout(layoutId, imageViews[i], io.getThumbnail(fixedThumbnail.getName(), EThumbnailType.A), EThumbnailType.A, true);
						passedFixedImage = true;
					}
				
				// the thumbnails on the left and the right side
				} else {
					thumbnailType = EThumbnailType.B;
					
					// if the last thumbnail is fixed display the predecessor on the left and the fixed thumbnail in the center
					if ((i >= (thumbnailFiles.size() - 1)) && (fixedThumbnail != null)) {
						addImageViewToLayout(layoutId, imageViews[i], getBitmapWithSize(i, thumbnailType), thumbnailType, false);
						addImageViewToLayout(layoutId, imageViews[i + 1], io.getThumbnail(fixedThumbnail.getName(), EThumbnailType.A), EThumbnailType.A, true);
						
						// everything is done for now, so skip the rest of the cycle
						continue;
					}
				}
				
			// 3) the remaining ones are upcoming thumbnails in the bottom row
			} else {
				layoutId = R.id.row_bottom;
				thumbnailType = thumbnailSizesBottomRow.get(i - (currentThumbnailNo + offsetToBottomRow));
			}
			
			// finally add the image to the view
			// if we passed the fixed image skip one image view position for it
			if (passedFixedImage) {
				if (i < (imageViews.length - 1)) {
					addImageViewToLayout(layoutId, imageViews[i + 1], getBitmapWithSize(i, thumbnailType), thumbnailType, false);
				}
			} else {
				addImageViewToLayout(layoutId, imageViews[i], getBitmapWithSize(i, thumbnailType), thumbnailType, false);
			}
		}
	}

	private void addImageViewToLayout(int layoutId, ImageView iv, Bitmap bm, EThumbnailType thumbnailType, boolean highlightImage) {
		// highlight the thumbnail
		if (highlightImage) {
			iv.setPadding(thumbnailType.getPaddingLeft(), Values.THUMBNAIL_HIGHLIGHT_WIDTH, thumbnailType.getPaddingRight(), Values.THUMBNAIL_HIGHLIGHT_WIDTH);
			iv.setBackgroundResource(R.layout.highlight);
		// reset default values
		} else {
			iv.setPadding(thumbnailType.getPaddingLeft(), 0, thumbnailType.getPaddingRight(), 0);
			iv.setBackgroundColor(Values.THUMBNAIL_BACKGROUND_COLOR);
		}
	
		// add the thumbnail to the image view
		iv.setImageBitmap(bm);
		// add the image view to the layout
		((LinearLayout) findViewById(layoutId)).addView(iv);
	}
	
	private List<EThumbnailType> calculateThumbnailSizes(int thumbnailsToDisplay, boolean reverse) {
		List<EThumbnailType> thumbnailTypes = new ArrayList<EThumbnailType>();
		
		int pixelsToGo = 0;
		EThumbnailType currentThumbnailType = EThumbnailType.C;
		
		for (int i = 0; i < thumbnailsToDisplay; i++) {
			int currentThumbnailWidth = currentThumbnailType.getTotalWidth();
			
			// simple case: we have enough space so we just add the thumbnail with the given size
			if ((pixelsToGo + currentThumbnailWidth) <= 1196) {
				thumbnailTypes.add(currentThumbnailType);
				
				pixelsToGo += currentThumbnailWidth;
				
			// not enough room left: resize the thumbnail
			} else {
				// check how many we smaller thumbs we have
				if (getNumberOfDifferentThumbnailTypes(thumbnailTypes) > 2) {
					
					// try to break them up
					int firstBiggerThumbnail = thumbnailTypes.lastIndexOf(getNextBiggerThumbnailType(getNextBiggerThumbnailType(currentThumbnailType)));
					
					
					thumbnailTypes.set(firstBiggerThumbnail, getNextBiggerThumbnailType(currentThumbnailType));
					thumbnailTypes.add((firstBiggerThumbnail + 1), getNextBiggerThumbnailType(currentThumbnailType));
				
				// just split the last position
				} else {
					// continue with the next smaller thumbnail size
					currentThumbnailType = getNextSmallerThumbnailType(currentThumbnailType);
					
					thumbnailTypes.set((thumbnailTypes.size() - 1), currentThumbnailType);
					thumbnailTypes.add(currentThumbnailType);
				}

				pixelsToGo = calculatePixelsInRow(thumbnailTypes);
			}
		}
		
		// reverse the list if needed
		if (reverse) {
			 Collections.reverse(thumbnailTypes);
		}
		
		return thumbnailTypes;
	}
	
	private int calculatePixelsInRow(List<EThumbnailType> thumbnailTypes) {
		int sum = 0;
		
		// sum up all thumbnail widths
		for (EThumbnailType thumbnailType : thumbnailTypes) {
			sum += thumbnailType.getTotalWidth();
		}
		
		// and return them
		return sum;
	}
	
	private EThumbnailType getNextSmallerThumbnailType(EThumbnailType thumbnailType) {
		// return the next smaller thumbnail type
		switch (thumbnailType) {
			case A:
				return EThumbnailType.B;
			case B:
				return EThumbnailType.C;
			case C:
				return EThumbnailType.D;
			case D:
				return EThumbnailType.E;
			case E:
				return EThumbnailType.F;
			case F:
				return EThumbnailType.G;
			case G:
				return EThumbnailType.H;
			case H:
			case I:
			default:
				return EThumbnailType.I;
		}
	}
	
	private int getNumberOfDifferentThumbnailTypes(List<EThumbnailType> thumbnailTypes) {
		// return the next smaller thumbnail type
		return new HashSet<EThumbnailType>(thumbnailTypes).size();
	}
	
	private EThumbnailType getNextBiggerThumbnailType(EThumbnailType thumbnailType) {
		// return the next bigger thumbnail type
		switch (thumbnailType) {
			case A:
			case B:
				return EThumbnailType.A;
			case C:
				return EThumbnailType.B;
			case D:
				return EThumbnailType.C;
			case E:
				return EThumbnailType.D;
			case F:
				return EThumbnailType.E;
			case G:
				return EThumbnailType.F;
			case H:
				return EThumbnailType.G;
			case I:
				return EThumbnailType.H;
			default:
				return EThumbnailType.I;
		}
	}
	
	private Bitmap getBitmapWithSize(int thumbnailNumber, EThumbnailType thumbnailType) {
		// return the thumbnail from the file system or from a list with the given number and size
		switch (thumbnailType) {
			case A:
				return io.getThumbnail(thumbnailFiles.get(thumbnailNumber).getName(), EThumbnailType.A);
			case B:
				return io.getThumbnail(thumbnailFiles.get(thumbnailNumber).getName(), EThumbnailType.B);
			case C:
				return io.getThumbnail(thumbnailFiles.get(thumbnailNumber).getName(), EThumbnailType.C);
			case D:
				return thumbnailsD.get(thumbnailNumber);
			case E:
				return thumbnailsE.get(thumbnailNumber);
			case F:
				return thumbnailsF.get(thumbnailNumber);
			case G:
				return thumbnailsG.get(thumbnailNumber);
			case H:
				return thumbnailsH.get(thumbnailNumber);
			case I:
				return thumbnailsI.get(thumbnailNumber);
			default:
				return null;
		}
	}

	@Override
	public void fixOrReleaseImage() {
		// first remove all thumbnails from the main section and redraw the in the correct order
		((LinearLayout) findViewById(R.id.main_section)).removeAllViews();
		// set the image in the right
		if (currentThumbnailNo > 0) {
			addImageViewToLayout(R.id.main_section, imageViews[(currentThumbnailNo - 1)], getBitmapWithSize((currentThumbnailNo - 1), EThumbnailType.B), EThumbnailType.B, false);
		}
		
		// no image fixed - fix the current one
		if (fixedThumbnail == null) {
			// set the fixed thumbnail
			fixedThumbnail = thumbnailFiles.get(currentThumbnailNo);
			// and remove it from all lists
			removeThumbnailFromListsAtLocation(currentThumbnailNo);

			// set and highlight the fixed thumbnail
			addImageViewToLayout(R.id.main_section, imageViews[currentThumbnailNo], io.getThumbnail(fixedThumbnail.getName(), EThumbnailType.A), EThumbnailType.A, true);
			// finally set the image on the left
			// (btw. it's the current picture because we already removed the fixed one from the lists)
			if (currentThumbnailNo < thumbnailFiles.size()) {
				addImageViewToLayout(R.id.main_section, imageViews[(currentThumbnailNo + 1)], getBitmapWithSize((currentThumbnailNo), EThumbnailType.B), EThumbnailType.B, false);
			}
			
		// store the fixed image at the current position and release it
		} else {
			// insert the fixed thumbnail into all lists at the current position,
			insertThumbnailIntoListsAtLocation(currentThumbnailNo, fixedThumbnail);
			
			// set the released thumbnail and remove the highlighting
			addImageViewToLayout(R.id.main_section, imageViews[currentThumbnailNo], io.getThumbnail(fixedThumbnail.getName(), EThumbnailType.A), EThumbnailType.A, false);
			// finally set the image on the left
			if (currentThumbnailNo < (thumbnailFiles.size() - 1)) {
				addImageViewToLayout(R.id.main_section, imageViews[(currentThumbnailNo + 1)], getBitmapWithSize((currentThumbnailNo + 1), EThumbnailType.B), EThumbnailType.B, false);
			}
			
			// and release the fixed thumbnail
			fixedThumbnail = null;
		}
	}
	
	private void removeThumbnailFromListsAtLocation(int location) {
		// remove a thumbnail at the given location from all lists
		thumbnailFiles.remove(location);
		thumbnailsD.remove(location);
		thumbnailsE.remove(location);
		thumbnailsF.remove(location);
		thumbnailsG.remove(location);
		thumbnailsH.remove(location);
		thumbnailsI.remove(location);
	}
	
	private void insertThumbnailIntoListsAtLocation(int location, File thumbnail) {
		// insert a thumbnail at the given location into all lists
		thumbnailFiles.add(location, thumbnail);
		thumbnailsD.add(location, io.getThumbnail(thumbnail.getName(), EThumbnailType.D));
		thumbnailsE.add(location, io.getThumbnail(thumbnail.getName(), EThumbnailType.E));
		thumbnailsF.add(location, io.getThumbnail(thumbnail.getName(), EThumbnailType.F));
		thumbnailsG.add(location, io.getThumbnail(thumbnail.getName(), EThumbnailType.G));
		thumbnailsH.add(location, io.getThumbnail(thumbnail.getName(), EThumbnailType.H));
		thumbnailsI.add(location, io.getThumbnail(thumbnail.getName(), EThumbnailType.I));
	}
	
	@Override
	public void startExternalProgram(EProgram program) {
		// TODO start various programs here
		switch (program) {
			case PROG_1:
				makeToast("Start Program 1");	
				break;
			case PROG_2:
				makeToast("Start Program 2");	
				break;
			default:
				;
		}
	}
	
	private void makeToast(String msg) {
		// displays a Toast on the screen
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
}