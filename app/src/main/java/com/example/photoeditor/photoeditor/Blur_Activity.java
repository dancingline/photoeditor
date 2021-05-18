package com.example.photoeditor.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import org.opencv.android.Utils;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;


public class Blur_Activity extends AppCompatActivity{

    private static final String  TAG  = "Blur_Activity";
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
    PhotoView blurDisplay;
    int process=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        blurDisplay = findViewById(R.id.blurDisplay);
//        blurDisplay.setMaxHeight((int)(getIntent().getExtras().getInt("height")*0.89f));
//        blurDisplay.setMaxWidth((int)(getIntent().getExtras().getInt("width")*0.89f));
        blurDisplay.setImageBitmap(textBit);

        ((SeekBar)findViewById(R.id.blurBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                process = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int r = process*textBit.getWidth()/100;
                if (r % 2 == 0) r++;
                blurDisplay.setImageBitmap(blur_Bitmap(r));  //这个参数怎么选择才合理
            }
        });

        ImageView saveDrawIcon = (ImageView)findViewById(R.id.saveOptionBlur);
        saveDrawIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{saveImage();}catch (Exception e){e.printStackTrace();}
            }
        });
        ImageView cancelIcon = (ImageView)findViewById(R.id.cancelOptionBlur);
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        Button saveChangesButton = (Button)findViewById(R.id.save_changes_button_Blur);
        saveChangesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saveBitmap();
//                Image_Display_Activity.bm=((BitmapDrawable)blurDisplay.getDrawable()).getBitmap();
//                (Image_Display_Activity.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
//                Image_Display_Activity.iHeight = textBit.getHeight();       //高度都是一样的我就不改变量了
                Toast.makeText(getApplicationContext(),"Changes Applied",Toast.LENGTH_SHORT).show();
            }
        });

        Button clearBlurButton = (Button)findViewById(R.id.clearBlurButton);
        clearBlurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //(dv.mCanvas).drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                blurDisplay.setImageBitmap(textBit);
                process = 0;
                ((SeekBar)findViewById(R.id.blurBar)).setProgress(0);
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
        Image_Display_Activity.bm = ((BitmapDrawable)blurDisplay.getDrawable()).getBitmap();
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

    Bitmap blur_Bitmap(int r){
        Bitmap ret = textBit.copy(textBit.getConfig(), true);

        // Bitmap转为Mat
        Mat src = new Mat(ret.getHeight(), ret.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(ret, src);

        // 高斯模糊方法
        Imgproc.GaussianBlur(src, src, new Size(r, r), r/2.57f, r/2.57f);
        // 参数依次为源图像、目标图像、卷积核大小和两个方向的σ
        // 关于两个σ的参数是网上找的

        // Mat转Bitmap
        Bitmap processedImage = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, processedImage);

        return processedImage;
        //return ret;
    }

    Bitmap blur_Bitmap2(int radius){
        Bitmap ret = textBit.copy(textBit.getConfig(), true);
        // 下面把bitmap转换为byte
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ret.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] pix = baos.toByteArray();


        int w = textBit.getWidth(), h = textBit.getHeight();
        float PI = 3.14f;

        float sigma =  1.0f * radius / 2.57f;	//2.57 * sigam半径之后基本没有贡献 所以取sigma为 r / 2.57
        float deno  =  1.0f / (sigma * (float)sqrt(2.0f * PI));
        float nume  = -1.0f / (2.0f * sigma * sigma);

        //高斯分布产生的数组
	    float[] gaussMatrix = new float[radius + radius + 1];
        float gaussSum = 0.0f;
        for (int i = 0, x = -radius; x <= radius; ++x, ++i)
        {
            float g = deno * (float)exp(1.0f * nume * x * x);

            gaussMatrix[i] = g;
            gaussSum += g;
        }

        //归1化
        int len = radius + radius + 1;
        for (int i = 0; i < len; ++i)
            gaussMatrix[i] /= gaussSum;

        //临时存储 一行的数据
//	    byte[] rowData  = new byte[w];
//	    byte[] listData = new byte[h];

        //x方向的模糊
        for (int y = 0; y < h; ++y)
        {
            //拷贝一行数据
            //memcpy(rowData, pix + y * w, sizeof(int) * w);

            for (int x = 0; x < w; ++x)
            {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;

                for (int i = -radius; i <= radius; ++i)
                {
                    int k = x + i;

                    if (0 <= k && k < w)
                    {
                        //得到像素点的rgb值
//                        int color = rowData[k];
//                        int cr = (color & 0x00ff0000) >> 16;
//                        int cg = (color & 0x0000ff00) >> 8;
//                        int cb = (color & 0x000000ff);

                        r += pix[y*4*w+k*4] * gaussMatrix[i + radius];
                        g += pix[y*4*w+k*4+1] * gaussMatrix[i + radius];
                        b += pix[y*4*w+k*4+2] * gaussMatrix[i + radius];

                        gaussSum += gaussMatrix[i + radius];
                    }
                }

//                int cr = (int)(r / gaussSum);
//                int cg = (int)(g / gaussSum);
//                int cb = (int)(b / gaussSum);
//
//                pix[y * w + x] = cr << 16 | cg << 8 | cb | 0xff000000;
                pix[y*4*w+x*4] = (byte)(r / gaussSum);
                pix[y*4*w+x*4+1] = (byte)(g / gaussSum);
                pix[y*4*w+x*4+2] = (byte)(b / gaussSum);
            }
        }
        //y方向的模糊
        for (int x = 0; x < w; ++x)
        {
            //拷贝 一列 数据
//            for (int y = 0; y < h; ++y)
//                listData[y] = pix[y * w + x];

            for (int y = 0; y < h; ++y)
            {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;

                for (int j = -radius; j <= radius; ++j)
                {
                    int k = y + j;

                    if (0 <= k && k < h)
                    {
//                        int color = listData[k];
//                        int cr = (color & 0x00ff0000) >> 16;
//                        int cg = (color & 0x0000ff00) >> 8;
//                        int cb = (color & 0x000000ff);

                        r += pix[k*4*w+x*4] * gaussMatrix[j + radius];
                        g += pix[k*4*w+x*4+1] * gaussMatrix[j + radius];
                        b += pix[k*4*w+x*4+2] * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }

//                int cr = (int)(r / gaussSum);
//                int cg = (int)(g / gaussSum);
//                int cb = (int)(b / gaussSum);

//                pix[y * w + x] = cr << 16 | cg << 8 | cb | 0xff000000;
                pix[y*4*w+x*4] = (byte)(r / gaussSum);
                pix[y*4*w+x*4+1] = (byte)(g / gaussSum);
                pix[y*4*w+x*4+2] = (byte)(b / gaussSum);
            }
        }
        return BitmapFactory.decodeByteArray(pix, 0, pix.length);        //将byte转换成bitmap并返回
    }

}
