package com.example.digidiet;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.logging.Logger;

public class ChooseModel extends AppCompatActivity {

    //button for each Model
    private Button modelNetV2;
    private Button efficientNetB0;
    private Button inceptionV3;

    //for permission requests
    public static final int REQUEST_PERMISSION = 300;

    //request code for permission requests to the os for image
    public static final int REQUEST_IMAGE = 100;

    //Will hold URI of image obtained from camera
    private Uri imageURI;

    //String to send to next activity that describes the chosen classifier
    private String chosen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_model);

       // request permission to use the camera on the user's phone
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }

        // request permission to write data (aka images) to the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        // request permission to read data (aka images) from the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }


        //onclick for inception model
        inceptionV3 = (Button)findViewById(R.id.inceptionv3);
        inceptionV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fileName in assets
                chosen = "model_inception.tflite";
                openCameraIntent();
            }
        });

    }

    private void openCameraIntent() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        // tell camera where to store the resulting picture
        imageURI = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // start camera, and wait for it to finish
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    // checks that the user has allowed all the required permission of read and write and camera. If not, notify the user and close the application
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(),"This application needs read, write, and camera permissions to run. Application now closing.",Toast.LENGTH_LONG);
                System.exit(0);
            }
        }
    }


    // dictates what to do after the user takes an image, selects and image, or crops an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK){
            try{
                Uri sourceURI = imageURI;
                Uri destURI = Uri.fromFile(new File(getCacheDir(),"cropped"));

                Crop.of(sourceURI,destURI).asSquare().start(ChooseModel.this);
            }catch (Exception e){
                throw e;
            }
        }

        // if cropping acitivty is finished, get the resulting cropped image uri and send it to 'Classify' activity
        else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK){
            imageURI = Crop.getOutput(data);
            Intent intent = new Intent(ChooseModel.this,Classify.class);

            intent.putExtra("imageURI",imageURI);
            intent.putExtra("modelChosen",chosen);

            startActivity(intent);
        }

    }

}