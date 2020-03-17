package com.example.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.ocr.MainActivity.CHOOSE_PHOTO;
import static com.example.ocr.MainActivity.CROP_PHOTO;
import static com.example.ocr.MainActivity.TAKE_PHOTO;

public class Main2Activity extends AppCompatActivity {

    private Uri imageUri;//图片uri

    private Bundle bundle;
    private int choice;
    private File outputImageDir;
    private File outputTextDir;
    private File outputImage;
    private File outputText;
    private String fileName;

    private ImageView picture;
    private TextView result;
    private ProgressBar progressBar;
    private TextView textView2;

    private String lang = "eng+chi_sim";
    private String tessData = "tessdata";

    private long time;//用时
    private int meanConfidence;//置信度

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String s = msg.obj.toString();
            if(s.isEmpty()){
                s = "No results!";
            }
            result.setText(s);
            time = (System.currentTimeMillis()-time)/1000;
            textView2.setText("(用时:"+time+"s\t置信度:"+meanConfidence+"%)");
            progressBar.setVisibility(View.GONE);

            saveText(s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        picture = findViewById(R.id.picture);
        result = findViewById(R.id.result);
        progressBar = findViewById(R.id.progressBar);
        textView2 = findViewById(R.id.textView2);

        bundle = this.getIntent().getExtras();
        choice = bundle.getInt("id");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//状态栏黑色图标
            View decorView = getWindow().getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(vis);
            }
        }

        prepareTesseract();

        switch (choice){
            case TAKE_PHOTO:{
                openCamera();
                break;
            }
            case CHOOSE_PHOTO:{
                openAlbum();
                break;
            }
        }
    }

    protected void NewFile(){//创建路径，创建文件
        outputImageDir = new File(getExternalCacheDir()+"/Images/");
        outputTextDir = new File(getExternalCacheDir()+"/Results/");

        if (!outputImageDir.exists()) {
            outputImageDir.mkdirs();
        }

        if (!outputTextDir.exists()) {
            outputTextDir.mkdirs();
        }

        fileName = System.currentTimeMillis()+"";
        outputImage = new File(outputImageDir,fileName+".jpg");
        outputText = new File(outputTextDir,fileName+".txt");

        try
        {
            if(outputImage.exists())
            {
                outputImage.delete();
            }
            outputImage.createNewFile();

            if(outputText.exists())
            {
                outputText.delete();
            }
            outputText.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void saveImage(Uri imageUri){
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            NewFile();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(outputImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void saveText(String s){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(s.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void openCamera(){//打开相机
        NewFile();

        if(Build.VERSION.SDK_INT>=24) {//Android 7.0
            imageUri= FileProvider.getUriForFile(Main2Activity.this,"com.example.ocr.fileProvider",outputImage);
        }
        else {
            imageUri= Uri.fromFile(outputImage);
        }
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    protected void openAlbum(){//打开相册
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    protected void cropPicture(){//裁剪图片
        Intent intent=new Intent("com.android.camera.action.CROP");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//否则无法加载图片
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.setDataAndType(imageUri,"image/*");
        intent.putExtra("scale",true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case TAKE_PHOTO:{//通过imageUri获取相机拍摄的原图
                    cropPicture();
                    break;
                }
                case CHOOSE_PHOTO:{//通过Intent获取相册原图并重新存储到指定路径
                    imageUri = data.getData();
                    saveImage(imageUri);

                    if(Build.VERSION.SDK_INT>=24) {//Android 7.0
                        imageUri= FileProvider.getUriForFile(Main2Activity.this,"com.example.ocr.fileProvider",outputImage);
                    }
                    else {
                        imageUri= Uri.fromFile(outputImage);
                    }

                    cropPicture();
                    break;
                }
                case CROP_PHOTO:{
                    Bitmap bitmap;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                        //bitmap = ImageFilter.grayscale(bitmap);
                        //bitmap = ImageFilter.binaryzation(bitmap);
                        final Bitmap final_bitmap = bitmap;
                        picture.setImageBitmap(final_bitmap);
                        time = System.currentTimeMillis();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String s = start_OCR(final_bitmap);
                                //对整段文字的格式处理
                                //s = s.replaceAll("\\n"," ");
                                //s = s.replaceAll("  ","\n\t");
                                //s = "\t" + s;
                                //convertToASCII(s);
                                Message msg = new Message();
                                msg.obj = s;
                                handler.sendMessage(msg);
                            }
                        });
                        thread.start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default:{
                    deleteEmptyFile();

                    Intent intent = new Intent();
                    intent.setClass(Main2Activity.this,MainActivity.class);
                    Main2Activity.this.startActivity(intent);
                }
            }
        }else {
            deleteEmptyFile();

            Intent intent = new Intent();
            intent.setClass(Main2Activity.this,MainActivity.class);
            Main2Activity.this.startActivity(intent);
        }
    }

    private void deleteEmptyFile(){
        if(!fileName.isEmpty()){
            File emptyImageFile = new File(getExternalCacheDir()+"/Images/"+fileName+".jpg");
            File emptyTextFile = new File(getExternalCacheDir()+"/Results/"+fileName+".txt");

            if(emptyImageFile.exists()){
                emptyImageFile.delete();
            }
            if(emptyTextFile.exists()){
                emptyTextFile.delete();
            }
        }
    }

    private void prepareTesseract() {
        try {
            prepareDirectory(getExternalCacheDir()+ "/TesseractSample/" + tessData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(tessData);
    }

    private void prepareDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = getExternalCacheDir()+ "/TesseractSample/" + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String start_OCR(Bitmap bitmap) {
        TessBaseAPI tessBaseApi = null;
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tessBaseApi.init(getExternalCacheDir()+ "/TesseractSample" , lang);

        tessBaseApi.setImage(bitmap);
        String OCR_result = null;

        try {
            OCR_result = tessBaseApi.getUTF8Text();
            meanConfidence = tessBaseApi.meanConfidence();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tessBaseApi.end();
        return OCR_result;
        //result.setText(OCR_result);
    }

    public static void convertToASCII(String string) {//检查字符串ASCII
        StringBuilder sb = new StringBuilder();
        char[] ch = string.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            sb.append(Integer.valueOf(ch[i]).intValue()).append("  ");// 加空格
        }
        System.out.println(sb);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//返回主页
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setClass(Main2Activity.this,MainActivity.class);
            Main2Activity.this.startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
