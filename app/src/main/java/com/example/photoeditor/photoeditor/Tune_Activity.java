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

public class Tune_Activity extends AppCompatActivity {

    Bitmap textBit = Image_Display_Activity.bm;
    float cont = 1f;
    float bright = 0f;
    float sat = 1f;
    ImageView tuneDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);    //设置actionbar不显示标题 actionbar就是最顶上的那一栏

        tuneDisplay = (ImageView)findViewById(R.id.tunedDisplay);
        tuneDisplay.setImageBitmap(textBit);


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

        ((SeekBar)findViewById(R.id.brightnessBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {      //中间的i表示百分比数值 范围从0到100
                bright = ((255f/50f)*i)-255f;
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));    //重新加载绘制完的图像
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((SeekBar)findViewById(R.id.contrastBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cont = i*(0.01f);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((SeekBar)findViewById(R.id.saturationBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sat = (float)i/100f;
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((Button)findViewById(R.id.resetBrightness)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bright = 0f;
                ((SeekBar)findViewById(R.id.brightnessBar)).setProgress(50);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }
        });

        ((Button)findViewById(R.id.resetContrast)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cont=1f;
                ((SeekBar)findViewById(R.id.contrastBar)).setProgress(10);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }
        });

        ((Button)findViewById(R.id.resetSaturation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sat = 1f;
                ((SeekBar)findViewById(R.id.saturationBar)).setProgress(50);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }
        });
        ((Button)findViewById(R.id.save_changes_button_tune)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                Toast.makeText(getApplicationContext(),"Changes Applied", Toast.LENGTH_SHORT).show();
            }
        });
        ((ImageView)findViewById(R.id.saveTuneIcon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    saveImage();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        ((ImageView)findViewById(R.id.cancelTuneIcon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    private void saveBitmap() {
        Image_Display_Activity.bm = ((BitmapDrawable)tuneDisplay.getDrawable()).getBitmap();
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

    private Bitmap changeBitmapContrastBrightness(float contrast, float brightness, float saturation)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast,0,0,0,brightness,
                        0,contrast,0,0,brightness,
                        0,0,contrast,0,brightness,
                        0,0,0,1,0
                });
        Bitmap ret = Bitmap.createBitmap(textBit.getWidth(), textBit.getHeight(),textBit.getConfig());

        Canvas canvas = new Canvas(ret);    //将新图像绘制到ret上

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));       //设置亮度和对比度参数
        canvas.drawBitmap(textBit,0,0,paint);
        cm.setSaturation(saturation);       //设置饱和度参数会使上面设置的参数失效 因此亮度对比度和饱和度分两步进行
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(ret,0,0,paint);
        return ret;     //返回绘制好的位图
    }
}
