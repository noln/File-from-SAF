package com.mattfenlon.blogstuff.filefromsaf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button button = (Button) findViewById(R.id.go_button);
        button.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
			   
				// Kick off the SAF selector.
				performFileSearch();
					
			}
        });
	}
	
	private static final int READ_REQUEST_CODE = 42;

	public void performFileSearch() {

		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");

		startActivityForResult(intent, READ_REQUEST_CODE);
	}
	
	/** We're overriding this function, which is called when an Activity result is 
	 * returned to this class. */
	@Override
	public void onActivityResult(int requestCode, int resultCode,
		Intent resultData) {

		/** 	This conditional statement ensures that we only run when the SAF has 
		 *	returned us a result successfully (Activity.RESULT_OK) and that the 
		 *	request code matches the one we used to request a file via the SAF 
		 *	Activity (resultCode == READ_REQUEST_CODE). */
		if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

			Uri uri = null;		// Instantiate a Uri object to hold the image's Uri.
			
			/**	We need to make sure that the data returned isn't null, to prevent 
			 *	any nasty null pointer exceptions. */
			 if (resultData != null) {
				 
				uri = resultData.getData();
				 
			 	try{
				 	/** Here we keep it all in a try-catch in case, so we don't 
				 	 *	force-close if something doesn't go to plan. */
			 		
			 		/** This bit isn't needed; it just displays the image specified in the Uri in an 
			 		 * 	ImageView, as a visual confirmation that the selection via SAF has worked.
			 		 * 
			 		 *  Note: This isn't from the downloaded file, it's just a cached, scaled-down image.
			 		 *  
			 		 *  If you don't need this bit, you can cut FROM HERE:
			 		 */
			 		Bitmap the_image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
	            	int nh = (int) ( the_image.getHeight() * (512.0 / the_image.getWidth()) );
	            	Bitmap scaled = Bitmap.createScaledBitmap(the_image, 512, nh, true);
	            	ImageView thumbnail = (ImageView) findViewById(R.id.imageViewSelectedImage);
	            	thumbnail.setImageBitmap(scaled);
	            	/** TO HERE */
			 		
				 	/**	This finds the location of the device's local storage (don't
				 	 *	assume that this will be /sdcard/!), and appends a hard-
				 	 *  coded string with a new subfolder, and gives the file that 
				 	 *  we are going to create a name.
				 	 * 
				 	 *  Note: You'll want to replace 'gdrive_image.jpg' with the 
				 	 *  filename that you fetch from Drive if you want to preserve 
				 	 *  the filename. That's out of the scope of this post. */ 
				 	String output_path = Environment.getExternalStorageDirectory() 
				 							+ "/MyNewFolder/gdrive_image.jpg";
				 	
				 	
				 	
				 	// Create the file in the location that we just defined.
				 	File oFile = new File(output_path);
				 	
				 	/**	Create the file if it doesn't exist; be aware that if it 
				 	 *	does, we'll be overwriting it further down. */
				 	if (!oFile.exists()) {
					 	/**	Note that this isn't just mkdirs; that would make our 
					 	 *	file into a directory! The 'getParentFile()' bit ensures
					 	 *	that the tail end remains a File. */
				 		oFile.getParentFile().mkdirs();
				 		oFile.createNewFile();	    				
				 	}
				 	
				 	InputStream iStream = this.getContentResolver().openInputStream(uri);
				 	
				 	/**	Create a byte array to hold the content that exists at the 
				 	 *	Uri we're interested in; this preserves all of the data that
				 	 *	exists within the file, including any JPEG meta data. If 
				 	 *	you punt this straight to a Bitmap object, you'll lose all 
				 	 *	of that.
				 	 *	
				 	 *	Note: This is reallt the main point of this entire post, as 
				 	 *	you're getting ALL OF THE DATA from the source file, as 
				 	 *	is. */
				 	byte[] inputData = getBytes(iStream);
				 	
				 	writeFile(inputData,output_path);
				 	
				} catch (Exception e){
					/** You'll have to forgive the lazy exception handling here...
					 * I'm keeping it clean for the sake of the post length! */
					e.printStackTrace();
				}
			 }

		}

	}

	/** This function puts everything in the provided InputStream into a byte array
	 *	and returns it to the calling function. */
	public byte[] getBytes(InputStream inputStream) throws IOException {
		
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		
		int len = 0;
		
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		
		return byteBuffer.toByteArray();
	}

	/**	This function rewrites the byte array to the provided filename.
	 *	
	 *	Note: A String, NOT a file object, though you could easily tweak it to do 
	 *	that. */
	public void writeFile(byte[] data, String fileName) throws IOException{
		FileOutputStream out = new FileOutputStream(fileName);
		out.write(data);
		out.close();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
