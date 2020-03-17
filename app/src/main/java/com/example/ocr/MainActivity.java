package com.example.ocr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int CROP_PHOTO = 3;

    private ImageButton history;
    private ImageButton album;
    private ImageButton camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        history = findViewById(R.id.history);
        album = findViewById(R.id.album);
        camera = findViewById(R.id.camera);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//状态栏白色图标
            View decorView = getWindow().getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                vis &= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(vis);
            }
        }

        newFile();//否则在初次打开时，打开历史记录会有问题

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Main3Activity.class);

                MainActivity.this.startActivity(intent);
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Main2Activity.class);

                Bundle bundle = new Bundle();
                bundle.putInt("id",CHOOSE_PHOTO);
                intent.putExtras(bundle);

                MainActivity.this.startActivity(intent);
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Main2Activity.class);

                Bundle bundle = new Bundle();
                bundle.putInt("id",TAKE_PHOTO);
                intent.putExtras(bundle);

                MainActivity.this.startActivity(intent);
            }
        });
    }

    private long mExitTime;//第一次点击事件发生的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//点击两次返回退出app
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再返回以退出", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);// 设置Intent动作
                intent.addCategory(Intent.CATEGORY_HOME);// 设置Intent种类
                MainActivity.this.startActivity(intent);// 将Intent传递给Activity
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private File outputImageDir;
    private File outputTextDir;

    protected void newFile(){
        outputImageDir = new File(getExternalCacheDir()+"/Images/");
        outputTextDir = new File(getExternalCacheDir()+"/Results/");

        if (!outputImageDir.exists()) {
            outputImageDir.mkdirs();
        }

        if (!outputTextDir.exists()) {
            outputTextDir.mkdirs();
        }
    }
}
