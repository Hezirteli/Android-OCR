package com.example.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main4Activity extends AppCompatActivity {

    private ImageView picture;
    private TextView result;
    private String id;

    private File getImageFile;
    private File getTextFile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        picture = findViewById(R.id.picture);
        result = findViewById(R.id.result);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//状态栏黑色图标
            View decorView = getWindow().getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(vis);
            }
        }

        Intent intent = getIntent();

        //HistoryItem item = intent.getParcelableExtra("HistoryItem");
        //picture.setImageBitmap(item.getImage());
        //result.setText(item.getText());

        id = intent.getStringExtra("id");
        setImage(id);
        setText(id);
    }

    protected void setImage(String id){
        getImageFile = new File(getExternalCacheDir()+"/Images/"+id+".jpg");
        if(Build.VERSION.SDK_INT>=24) {//Android 7.0
            imageUri= FileProvider.getUriForFile(Main4Activity.this,"com.example.ocr.fileProvider",getImageFile);
        }
        else {
            imageUri= Uri.fromFile(getImageFile);
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            picture.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void setText(String id){
        String content="";
        getTextFile = new File(getExternalCacheDir()+"/Results/"+id+".txt");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getTextFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        try {
            String line;
            //分行读取
            while (( line = br.readLine()) != null) {
                content += line + "\n";
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        result.setText(content);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//返回主页
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setClass(Main4Activity.this,Main3Activity.class);
            Main4Activity.this.startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

