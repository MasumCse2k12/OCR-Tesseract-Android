package com.istl.samples.faceverification;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.imperialsoupgmail.faceverification.R;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.ArrayList;


public class BackNidCapture extends AppCompatActivity {
    Bitmap image;
    AsyncTask<Bitmap, Void, String> theTask;
    TextView OCRTextView ;
    ImageView imageView;
    String targetPath;
    TextView oCRbutton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.back_nid_capture);

        //init image
        imageView = (ImageView) findViewById(R.id.imageView);
        oCRbutton = findViewById(R.id.OCRbutton);
//        OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bank Account Registration");
        getSupportActionBar().setSubtitle("Taking Back NID image");

    }


    public void submitBackImage(View view){
        Intent intent = new Intent (BackNidCapture.this,FaceVerificationApplication.class);
                startActivity(intent);
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent (MainActivity.this, BackNidCapture.class);
//                startActivity(intent);
//            }
//        });
    }


    public void pickImage(View view){

        /*
        ImagePicker.with(this)
                .setCameraOnly(true)
                .setMaxSize(1)
                .setMultipleMode(false)
                .start();
                */

        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            // do your logic here...

            if (images.size() > 0)
            {
                String path = images.get(0).getPath();
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                image = BitmapFactory.decodeFile(path,bmOptions);
                //bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                imageView.setImageBitmap(image);
                oCRbutton.setClickable(true);

            }
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                image = BitmapFactory.decodeFile(resultUri.getPath(),bmOptions);
                targetPath = resultUri.getPath();
                imageView.setImageBitmap(image);
                oCRbutton.setClickable(true);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        // THIS METHOD SHOULD BE HERE so that ImagePicker works with fragment
    }
}
