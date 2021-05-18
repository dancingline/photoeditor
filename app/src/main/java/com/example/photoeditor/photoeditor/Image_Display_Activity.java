package com.example.photoeditor.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Image_Display_Activity extends AppCompatActivity {

    static Bitmap bm= BitmapFactory.decodeFile(MainActivity.mCurrentPhotoPath);     //既然这里已经把整个图加载了，为什么下面还要费劲去抽样
    static float vH=0,vW=0;
    static BitmapFactory.Options bmOptions;
    private final static String TAG = "DEBUG_BOTTOM_NAV_UTIL";
    static PhotoView imageDisplay;
    static float iHeight=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_image_display);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageDisplay = (PhotoView) findViewById(R.id.imageDisplay);

        final float targetW = getIntent().getExtras().getInt("width");      //显示区的大小
        final float targetH = getIntent().getExtras().getInt("height");

        /**
        *  以下用来加载图片
        *  特别是大图片
        *  获取原图放缩后再加载位图
        **/

        bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;        //当这个参数被设置为true时，表示只加载图片的大小  存储在outWidth和outHeight两个成员里
        BitmapFactory.decodeFile(MainActivity.mCurrentPhotoPath,bmOptions);     //这个类用来把图片加载到内存 这里只加载大小
        float photoW = bmOptions.outWidth;      //获取图片宽度
        float photoH = bmOptions.outHeight;     //获取图片高度

        // 缩小图片使其能够显示
        {
            vH = targetH*(0.89f);       //实际高度
            vW = (targetH*(0.89f) / (bm.getHeight())) * (bm.getWidth());        //实际宽度
            if(vW>targetW){     //这一步会使长宽比发生变化
                vW = targetW;
                vH=(targetW/(bm.getWidth()))*(bm.getHeight());
            }
        }
        float scaleFactor = Math.min(photoW/vW,photoH/vH);      //放缩比

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int)scaleFactor;      //如果这个值大于1 将会从原图中抽样出一个更小的图 以节约内存
        bmOptions.inPurgeable=true;
        bm = rotImage(BitmapFactory.decodeFile(MainActivity.mCurrentPhotoPath,bmOptions));      //这里真正在加载图片到内存 还顺便旋转了一下
        bmOptions.inJustDecodeBounds=true;
        iHeight=bmOptions.outHeight;        //这个值跟上面用的时候应该没有变化

        imageDisplay.setImageBitmap(bm);

        final BottomNavigationView optionNavigationView = (BottomNavigationView)findViewById(R.id.optionNavigation);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView)optionNavigationView.getChildAt(0);
        try{
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView,false);
            shiftingMode.setAccessible(false);
            for (int i=0;i<menuView.getChildCount();i++){
                BottomNavigationItemView item = (BottomNavigationItemView)menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }
        }catch (NoSuchFieldException e){
            Log.d(TAG,"Unable to get shift mode field");
        }catch (IllegalAccessException e){
            Log.d(TAG, "Unable to change value of shift mode");
        }

        optionNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
//                    case R.id.action_draw:
//                        Log.i(TAG, ": action_draw");
//                        Intent drawIntent = new Intent(Image_Display_Activity.this, Draw_Activity.class);
//                        drawIntent.putExtra("height",targetH);
//                        drawIntent.putExtra("width",targetW);
//                        ActivityOptionsCompat optionsDraw = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
//                        ActivityCompat.startActivity(Image_Display_Activity.this, drawIntent,optionsDraw.toBundle());
//                        break;
                    case R.id.action_blur:
                        Log.i(TAG, ": action_blur");
                        Intent blurIntent = new Intent(Image_Display_Activity.this, Blur_Activity.class);
                        blurIntent.putExtra("height",targetH);
                        blurIntent.putExtra("width",targetW);
                        ActivityOptionsCompat optionsBlur = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, blurIntent,optionsBlur.toBundle());
                        break;
                    case R.id.action_sharpen:
                        Log.i(TAG, ": action_sharpen");
                        Intent sharpenIntent = new Intent(Image_Display_Activity.this, Sharpen_Activity.class);
                        sharpenIntent.putExtra("height",targetH);
                        sharpenIntent.putExtra("width",targetW);
                        ActivityOptionsCompat optionsSharpen = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, sharpenIntent,optionsSharpen.toBundle());
                        break;
                    case R.id.action_rotateCrop:
                        Log.i(TAG, ": action_rotateCrop");
                        Intent rotIntent = new Intent(Image_Display_Activity.this, Rotate_Crop_Activity.class);
                        rotIntent.putExtra("height",targetH);
                        rotIntent.putExtra("width",targetW);
                        ActivityOptionsCompat optionsRot = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, rotIntent,optionsRot.toBundle());
                        break;
                    case R.id.action_tune:
                        Log.i(TAG, ": action_tune");
                        Intent tuneIntent = new Intent(Image_Display_Activity.this, Tune_Activity.class);
                        tuneIntent.putExtra("height",targetH);
                        tuneIntent.putExtra("width",targetW);
                        tuneIntent.putExtra("iHeight",iHeight);     //为什么只有调整需要把它传过去
                        ActivityOptionsCompat optionsTune = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, tuneIntent,optionsTune.toBundle());
                        break;
                    case R.id.action_color:
                        Log.i(TAG, ": action_color");
                        Intent colorIntent = new Intent(Image_Display_Activity.this, Color_Activity.class);
                        colorIntent.putExtra("height",targetH);
                        colorIntent.putExtra("width",targetW);
                        //colorIntent.putExtra("iHeight",iHeight);     //为什么只有调整需要把它传过去
                        ActivityOptionsCompat optionsColor = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, colorIntent,optionsColor.toBundle());
                        break;
                }
                return true;
            }
        });

        ImageView saveDisplayImage = (ImageView) findViewById(R.id.saveImageDisplay);
        saveDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    saveImage();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        ImageView cancelDisplayImage = (ImageView)findViewById(R.id.cancelImageDisplay);
        cancelDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    private void saveImage()throws Exception{
        FileOutputStream fOut = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_"+timeStamp+"_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName,".png",file2);

        try{
            fOut = new FileOutputStream(file);
        }catch (Exception e){e.printStackTrace();}
        bm.compress(Bitmap.CompressFormat.PNG,100,fOut);
        try{
            fOut.flush();
        }catch (Exception e){e.printStackTrace();}
        try{fOut.close();}catch (IOException e){e.printStackTrace();}
        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());}
        catch (FileNotFoundException e){e.printStackTrace();}

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri = Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(),"Image Saved to Pictures",Toast.LENGTH_SHORT).show();
    }

    private Bitmap rotImage(Bitmap bitmap){
        try {
            ExifInterface exif = new ExifInterface(MainActivity.mCurrentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);

            Matrix matrix = new Matrix();
            if(orientation==3){matrix.postRotate(180);}
            else if(orientation==6){matrix.postRotate(90);}
            else if (orientation==8){matrix.postRotate(270);}

            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            return bitmap;
        }catch (IOException e){e.printStackTrace(); return null;}
    }
}
