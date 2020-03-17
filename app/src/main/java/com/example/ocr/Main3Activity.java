package com.example.ocr;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main3Activity extends AppCompatActivity {

    private File imageDir;
    private File textDir;
    private File[] imageFileList;
    private File[] textFileList;
    private String[] imageFileName;
    private String[] textFileName;

    private File getImageFile;
    private File getTextFile;
    private Uri imageUri;

    private List<HistoryItem> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//状态栏白色图标
            View decorView = getWindow().getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                vis &= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(vis);
            }
        }

        searchAllFiles();
        getAllFileNames();
        //showFileList(imageFileList);
        //showFileList(textFileList);

        initList();

        //将数据源添加到适配器
        MyAdapter adapter = new MyAdapter(Main3Activity.this, R.layout.item, list);//绑定item子布局（图片+文字）
        ListView listView = findViewById(R.id.list);
        //将适配器中数据添加到ListView中
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryItem item = list.get(position);

                Intent intent = new Intent();
                intent.setClass(Main3Activity.this,Main4Activity.class);

                //尝试使用parcelable接口传递自定义类，但是intent传递数据大小不能超过1M，bitmap不能大于40K
                //intent.putExtra("HistoryItem", item);

                //传送文件名id，从cache中重新读取
                intent.putExtra("id",item.getId());
                Main3Activity.this.startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final HistoryItem item = list.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                builder.setMessage("确定删除此条记录？");
                builder.setTitle("提示");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File deleteImageFile = new File(getExternalCacheDir()+"/Images/"+item.getId()+".jpg");
                        File deleteTextFile = new File(getExternalCacheDir()+"/Results/"+item.getId()+".txt");
                        if(deleteImageFile.exists())
                            deleteImageFile.delete();
                        if(deleteTextFile.exists())
                            deleteTextFile.delete();

                        searchAllFiles();
                        getAllFileNames();
                        initList();
                        //将数据源添加到适配器
                        MyAdapter adapter = new MyAdapter(Main3Activity.this, R.layout.item, list);//绑定item子布局（图片+文字）
                        ListView listView = findViewById(R.id.list);
                        //将适配器中数据添加到ListView中
                        listView.setAdapter(adapter);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
                return true;
            }
        });

    }

    protected void searchAllFiles(){//获取所有图片、文字的文件
        imageDir = new File(getExternalCacheDir()+"/Images/");
        textDir = new File(getExternalCacheDir()+"/Results/");
        imageFileList = imageDir.listFiles();
        textFileList = textDir.listFiles();
        imageFileName = new String[imageFileList.length];
        textFileName = new String[textFileList.length];
    }

    protected void getAllFileNames(){
        if(imageFileList != null){
            for(int i=0;i<imageFileList.length;i++){
                String temp = imageFileList[i].getName();
                imageFileName[i] = temp.substring(0,temp.lastIndexOf('.'));
            }
        }

        if(textFileList !=null){
            for(int i=0;i<textFileList.length;i++){
                String temp = textFileList[i].getName();
                textFileName[i] = temp.substring(0,temp.lastIndexOf('.'));
            }
        }
    }

    protected void showFileList(File[] file){//测试查看获取到的文件
        if(file != null){
            for(int i=0;i<file.length;i++){
                System.out.println(file[i].getName());
            }
        }
    }

    protected void initList(){//读取文件名、图片、文字，初始化history list
        list.clear();

        for(int i=0;i<textFileList.length;i++){
            getImageFile = imageFileList[i];
            getTextFile = textFileList[i];

            if(Build.VERSION.SDK_INT>=24) {//Android 7.0
                imageUri= FileProvider.getUriForFile(Main3Activity.this,"com.example.ocr.fileProvider",getImageFile);
            }
            else {
                imageUri= Uri.fromFile(getImageFile);
            }

            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                String string = readFromTXT(getTextFile);
                HistoryItem item = new HistoryItem(textFileName[i],bitmap,string);
                list.add(item);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected String readFromTXT(File file){
        String content="";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
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

        return content;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//返回History
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setClass(Main3Activity.this,MainActivity.class);
            Main3Activity.this.startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
