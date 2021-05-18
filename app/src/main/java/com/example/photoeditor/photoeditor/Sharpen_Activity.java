package com.example.photoeditor.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sharpen_Activity extends AppCompatActivity {

    private static final String  TAG  = "Sharpen_Activity";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    Bitmap textBit = Image_Display_Activity.bm;
    PhotoView sharpenDisplay;
    int process=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharpen_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        sharpenDisplay = findViewById(R.id.sharpenDisplay);
        sharpenDisplay.setImageBitmap(textBit);

        ((SeekBar)findViewById(R.id.sharpenBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                process=i;
                float r = process/10f+4.8f;
                sharpenDisplay.setImageBitmap(sharpen_Bitmap(r));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                float r = process/10f+4.8f;
//                sharpenDisplay.setImageBitmap(sharpen_Bitmap(r));
            }
        });

        ImageView saveSharpenIcon = (ImageView)findViewById(R.id.saveOptionSharpen);
        saveSharpenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{saveImage();}catch (Exception e){e.printStackTrace();}
            }
        });

        ImageView cancelIcon = (ImageView)findViewById(R.id.cancelOptionSharpen);
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        Button saveChangesButton = (Button)findViewById(R.id.save_changes_button_Sharpen);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                Toast.makeText(getApplicationContext(),"Changes Applied",Toast.LENGTH_SHORT).show();
            }
        });

        Button clearSharpenButton = (Button)findViewById(R.id.clearSharpenButton);
        clearSharpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharpenDisplay.setImageBitmap(textBit);
                process= 0;
                ((SeekBar)findViewById(R.id.sharpenBar)).setProgress(0);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void saveBitmap() {
        Image_Display_Activity.bm = ((BitmapDrawable)sharpenDisplay.getDrawable()).getBitmap();
        (Image_Display_Activity.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
    }
    private void saveImage()throws Exception{
        saveBitmap();
        FileOutputStream fOut=null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_"+timeStamp+"_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName,".png",file2);

        try{
            fOut = new FileOutputStream(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        (Image_Display_Activity.bm).compress(Bitmap.CompressFormat.PNG,100,fOut);
        try{
            fOut.close();
        }catch (IOException e){e.printStackTrace();}
        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        }catch (FileNotFoundException e){e.printStackTrace();}

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri=Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(),"Image saved to pictures",Toast.LENGTH_SHORT).show();
    }

    Bitmap sharpen_Bitmap(float r){
        Bitmap ret = textBit.copy(textBit.getConfig(), true);

        Mat kernel = new Mat(3,3, CvType.CV_32FC1);  //CvType.CV_32FC1
        float[] sharper;
//        if(r<5f){
//            sharper = new float[]{0,0,0,0,1,0,0,0,0};
//        }
//        else {
            sharper = new float[]{0,-1,0,-1,r,-1,0,-1,0};
//        }
        kernel.put(0,0,sharper);
        Mat src = new Mat(ret.getHeight(), ret.getWidth(), CvType.CV_32FC1);
        Mat src2 = new Mat(ret.getHeight(), ret.getWidth(), CvType.CV_32FC1);
        Utils.bitmapToMat(ret,src);
        //Utils.bitmapToMat(ret,src2);
        //Imgproc.blur(src, src, new Size(3,3));
        Imgproc.filter2D(src,src,-1,kernel);
        //Core.addWeighted(src,1, src2, 1, 0, src);
        Bitmap processedImaage = Bitmap.createBitmap(src.cols(), src.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, processedImaage);
        return processedImaage;
    }
}
