package com.neoris.bcbabies.helper;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.neoris.bcbabies.R;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class SelectedImageActivity extends ActionBarActivity {
    // Flag to indicate the request of the next task to be performed
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;

    // The URI of photo taken from gallery
    private Uri mUriPhotoTaken;

    // File of the photo taken with camera
    private File mFilePhotoTaken;
public Button botonsin;
    // When the activity is created, set all the member variables to initial state.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image);

        botonsin = (Button) findViewById(R.id.button_take_a_photo);
        botonsin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager()) != null) {
                    // Save the photo taken to a temporary file.
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    try {
                        mFilePhotoTaken = File.createTempFile(
                                "IMG_",  /* prefix */
                                ".jpg",         /* suffix */
                                storageDir      /* directory */
                        );

                        // Create the File where the photo should go
                        // Continue only if the File was successfully created
                        if (mFilePhotoTaken != null) {
                            mUriPhotoTaken = FileProvider.getUriForFile(getApplication(), "com.neoris.bcbabies.fileprovider", mFilePhotoTaken);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);

                            // Finally start camera activity
                            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                        }
                    } catch (IOException e) {
                        setInfo(e.getMessage());
                    }
                }

            }
        });
    }

    // Save the activity state when it's going to stop.
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", mUriPhotoTaken);
    }

    // Recover the saved state when the activity is recreated.
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri");
    }

    // Deal with the result of selection of the photos and faces.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent();
                    intent.setData(Uri.fromFile(mFilePhotoTaken));
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
                default:
                break;
        }
    }

    // When the button of "Take a Photo with Camera" is pressed.
    //public void takePhoto(View view) {

    //}

    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }
}
