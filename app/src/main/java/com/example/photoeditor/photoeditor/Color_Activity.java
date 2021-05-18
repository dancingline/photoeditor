package com.example.photoeditor.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Color_Activity extends AppCompatActivity {

    Bitmap textBit = Image_Display_Activity.bm;
    float r = 0;
    float g = 0;
    float blue = 0;
    ImageView colorDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);    //设置actionbar不显示标题 actionbar就是最顶上的那一栏

        colorDisplay = (ImageView)findViewById(R.id.colorDisplay);
        colorDisplay.setImageBitmap(textBit);


        /**
         * 我并不知道比较实际高度和缩放以后的高度减400有什么用
         * 如果下面的if没有执行
         * 那tuneDisplay的大小应该是什么样的
         * 注释掉似乎也没有什么区别
         **/

//        if(getIntent().getExtras().getFloat("iHeight") > getIntent().getExtras().getFloat("height")-400) {
//            tuneDisplay.getLayoutParams().height=(int)(getIntent().getExtras().getFloat("height")-400);
//            tuneDisplay.requestLayout();
//        }

        ((SeekBar)findViewById(R.id.RBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {      //中间的i表示百分比数值 范围从0到100
                r = (i-50)*255/100;
                colorDisplay.setImageBitmap(changeBitmapColor(r, g, blue));    //重新加载绘制完的图像
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((SeekBar)findViewById(R.id.GBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                g = (i-50)*255/100;
                colorDisplay.setImageBitmap(changeBitmapColor(r, g, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((SeekBar)findViewById(R.id.BBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blue = (i-50)*255/100;
                colorDisplay.setImageBitmap(changeBitmapColor(r, g, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((Button)findViewById(R.id.resetR)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r = 0;
                ((SeekBar)findViewById(R.id.RBar)).setProgress(50);
                colorDisplay.setImageBitmap(changeBitmapColor(r, g, blue));
            }
        });

        ((Button)findViewById(R.id.resetG)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                g = 0;
                ((SeekBar)findViewById(R.id.GBar)).setProgress(50);
                colorDisplay.setImageBitmap(changeBitmapColor(r, g, blue));
            }
        });

        ((Button)findViewById(R.id.resetB)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blue = 0;
                ((SeekBar)findViewById(R.id.BBar)).setProgress(50);
                colorDisplay.setImageBitmap(changeBitmapColor(r, g, blue));
            }
        });
        ((Button)findViewById(R.id.save_changes_button_color)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                Toast.makeText(getApplicationContext(),"Changes Applied", Toast.LENGTH_SHORT).show();
            }
        });
        ((ImageView)findViewById(R.id.saveColorIcon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{saveImage();}catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        ((ImageView)findViewById(R.id.cancelColorIcon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    private void saveBitmap() {
        Image_Display_Activity.bm = ((BitmapDrawable)colorDisplay.getDrawable()).getBitmap();
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

    private Bitmap changeBitmapColor(float r, float g, float b)
    {
        ColorMatrix cm = new ColorMatrix(new float[]        //colormatrix的setrotate方法是旋转调色，一个通道不变另两个旋转，效果很奇怪
                {                                           //这里就直接给对应通道加倍了
                        1+r*256,0,0,0,0,
                        0,1+g,0,0,0,
                        0,0,1+b,0,0,
                        0,0,0,1,0
                });
        Bitmap ret = Bitmap.createBitmap(textBit.getWidth(), textBit.getHeight(),textBit.getConfig());
        Canvas canvas = new Canvas(ret);    //将新图像绘制到ret上
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(textBit,0,0,paint);
        return ret;     //返回绘制好的位图
    }
}
