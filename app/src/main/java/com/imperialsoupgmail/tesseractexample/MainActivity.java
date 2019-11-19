package com.imperialsoupgmail.tesseractexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.googlecode.tesseract.android.TessBaseAPI;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import pyxis.uzuki.live.mediaresizer.MediaResizer;
import pyxis.uzuki.live.mediaresizer.data.ImageResizeOption;
import pyxis.uzuki.live.mediaresizer.data.ResizeOption;
import pyxis.uzuki.live.mediaresizer.model.ImageMode;
import pyxis.uzuki.live.mediaresizer.model.MediaType;
import pyxis.uzuki.live.richutilskt.impl.F2;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    private TessBaseAPI mTess;
    String datapath = "";
    ProgressDialog progressDialog;
    AsyncTask<Bitmap, Void, String> theTask;
    TextView OCRTextView ;
    ImageView imageView;
    String targetPath;
    Mat imageMat;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.e("OpenCV", "OpenCV loaded successfully");
                    imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            System.loadLibrary(OpenCVLoader.OPENCV_VERSION_3_0_0);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init image
        imageView = (ImageView) findViewById(R.id.imageView);
        OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing image...");

        //initialize Tesseract API
//        String language = "eng";
        String language = "eng";
        datapath = getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));

        mTess.init(datapath, language);
    }

    public void processImage(View view){
        String OCRresult = null;
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (theTask != null)
                    theTask.cancel(true);
            }
        });


        // Compress before processing it
        ImageResizeOption resizeOption = new ImageResizeOption.Builder()
                .setImageProcessMode(ImageMode.ResizeAndCompress)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setCompressQuality(40)
                .build();

        File dir = new File(datapath + "/images/");
        dir.mkdirs();
        File file = new File(datapath + "/images/", "resize-"+System.currentTimeMillis()+".jpg");
        if (file.exists() == false)
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ResizeOption option = new ResizeOption.Builder()
                .setMediaType(MediaType.IMAGE)
                .setImageResizeOption(resizeOption)
                .setTargetPath(targetPath)
                .setOutputPath(file.getAbsolutePath())
                .setCallback(new F2<Integer, String>() {
                    @Override
                    public void invoke(Integer code, String output) {
                        Log.e("RESIZE", code + " -- " + output);


                        BitmapFactory.Options options = new BitmapFactory.Options();
                        //options.inSampleSize = 3;
                        Bitmap compressImage = BitmapFactory.decodeFile(output, options);

                        // Convert the image to black white before OCR
                        //Bitmap black = convertToBlackWhite(compressImage);

                        //imageView.setImageBitmap(black);



                        theTask = new ImageProcessTask().execute(convertToBlackWhite(compressImage));
                        //theTask = new ImageProcessTask().execute(compressImage);

                    }
                })
                .build();

        MediaResizer.process(option);

//        try{
//            theTask = new ImageProcessTask().execute(image);
//        }catch (Throwable throwable){
//
//        }



        //
    }

    public Bitmap convertToBlackWhite(Bitmap compressImage)
    {
        Log.d("CV", "Before converting to black");
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
//        Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);
        //Imgproc.medianBlur(imageMat, imageMat, 3);
        Imgproc.threshold(imageMat, imageMat, 0, 255, Imgproc.THRESH_OTSU);

        Bitmap newBitmap = compressImage;
        Utils.matToBitmap(imageMat, newBitmap);
        imageView.setImageBitmap(newBitmap);
        Log.d("CV", "After converting to black");


        return newBitmap;

    }

//    public void processImage(View view){
//        String OCRresult = null;
//        mTess.setImage(image);
//        OCRresult = mTess.getUTF8Text();
//        Toast.makeText(getBaseContext(),OCRresult, Toast.LENGTH_SHORT).show();
//        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
//        OCRTextView.setText(OCRresult);
//    }

    class ImageProcessTask extends AsyncTask<Bitmap, Void, String>
    {


        @Override
        protected String doInBackground(Bitmap... bitmaps) {

            Log.d("CV", "Starting ocr");

            String OCRresult = null;
            mTess.setImage(bitmaps[0]);

            OCRresult = mTess.getUTF8Text();
            return OCRresult;
        }

        @Override
        protected void onPostExecute(final String text) {
            super.onPostExecute(text);

            Log.d("CV", "OCR Done: " + text);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    OCRTextView.setText(text);
                }
            });
        }
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
                copyFiles();
        }
        if(dir.exists()) {
//            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        // THIS METHOD SHOULD BE HERE so that ImagePicker works with fragment
    }
}
