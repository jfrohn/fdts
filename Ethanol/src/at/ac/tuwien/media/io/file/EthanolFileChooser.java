package at.ac.tuwien.media.io.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.ac.tuwien.media.R;
import at.ac.tuwien.media.util.EthanolLogger;
import at.ac.tuwien.media.util.Value;
import at.ac.tuwien.media.util.exception.EthanolException;

/**
 * The {@link EthanolFileChooser} ...
 * 
 * @author jakob.frohnwieser@gmx.at
 */
public class EthanolFileChooser implements OnItemClickListener, OnClickListener {
	private List<File> directoryFiles;
	private File currentDirectory;
	private ListView listView;
	private Context parent;

	public EthanolFileChooser(final Context parent, String root) {
		this.parent = parent;
		directoryFiles = new ArrayList<File>();
		
		// set current directory to start with
		if (root == null || root.isEmpty()) {
			root = Value.SDCARD;
		}
		currentDirectory = new File(root);

		getSubdirectoriesAndImages();
		
		// show a dialog
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parent);
		alertDialogBuilder.setTitle(R.string.choose_folder);
		alertDialogBuilder.setAdapter(new EthanolDirectoryAdapter(parent, R.layout.file_chooser_item, directoryFiles), this);

		// add OK button
		alertDialogBuilder.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					Configuration.set(Value.CONFIG_IMAGE_FOLDER, currentDirectory.getAbsolutePath());
					EthanolLogger.addDebugMessage("Set new configuration folder "
							+ Configuration.get(Value.CONFIG_IMAGE_FOLDER));
					//TODO restart + format file list
				} catch (EthanolException e) {
					// we cannot do anything against it
					e.printStackTrace();
				}
				
				dialog.dismiss();
			}
		});

		// add Chancel button
		alertDialogBuilder.setNegativeButton(R.string.chancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		final AlertDialog alertDialog = alertDialogBuilder.create();
		listView = alertDialog.getListView();
		listView.setOnItemClickListener(this);	
		alertDialog.show();
	}
	
	@Override
	public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
		// check if current position is in boundaries
		if (position >= 0 && position < directoryFiles.size()) {
			final File selectedDirectory = directoryFiles.get(position);
			// must be directory or parent
			if (selectedDirectory.isDirectory() || selectedDirectory.getName().equals(Value.PARENT_FOLDER)) {
				// get selected folder
				currentDirectory = selectedDirectory.getName().equals(Value.PARENT_FOLDER) ?
						currentDirectory.getParentFile()
						: directoryFiles.get(position);

				// reload file list view
				getSubdirectoriesAndImages();
				listView.setAdapter(new EthanolDirectoryAdapter(parent, R.layout.file_chooser_item, directoryFiles));
			}
		}
	}

	private void getSubdirectoriesAndImages() {
		// clean old file list
		directoryFiles.clear();

		// list all files in the current directory
		final File[] files = currentDirectory.listFiles();
		// add them to the directory list
		if (files != null) {
			for (File file : files) {
				// filter only folders and images
				if (file.isDirectory()
						|| file.getName().matches(Value.REGEX_IMAGE)) {
					directoryFiles.add(file);
				}
			}
			
			// sort them
			Collections.sort(directoryFiles);
		}

		// add parent directory to the begin of the list
		if (currentDirectory.getParent() != null) {
			directoryFiles.add(0, new File(Value.PARENT_FOLDER));
		}
	}
	
	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// this method intentionally left blank
	}
	
	// Ethanol Directory Adapter Class
	public class EthanolDirectoryAdapter extends ArrayAdapter<File> {
		
		public EthanolDirectoryAdapter(final Context context, final int resourceId, final List<File> objects) {
			super(context, resourceId, objects);
		}
		
		@Override
        public View getView(final int position, final View convertView, final ViewGroup viewGroup) {
            final TextView textView = (TextView) super.getView(position, convertView, viewGroup);
            
            // get values for view
            final File directoryAtPosition = directoryFiles.get(position);
            final String text = directoryAtPosition == null ?
					Value.PARENT_FOLDER
					: directoryAtPosition.getName();
            final int iconId = text.equals(Value.PARENT_FOLDER) ?
				R.drawable.ic_back
				: text.matches(Value.REGEX_IMAGE) ?
						R.drawable.ic_image
						: R.drawable.ic_folder;
            
            // set values
            textView.setText(text);
            textView.setCompoundDrawablesWithIntrinsicBounds(parent.getResources().getDrawable(iconId), null, null, null );
 
            return textView;
        }

	}
}