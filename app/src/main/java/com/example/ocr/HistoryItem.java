package com.example.ocr;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class HistoryItem implements Parcelable {//为了在activity之间传递自定义类的对象，实现parcelable接口
    private String id;
    private Bitmap image;
    private String text;

    public HistoryItem(){
    }

    public HistoryItem(String id,Bitmap image,String text){
        this.id = id;
        this.image = image;
        this.text = text;
    }

    public String getId(){
        return id;
    }

    public Bitmap getImage(){
        return image;
    }

    public String getText(){
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //实现Parcelable接口的public void writeToParcel(Parcel dest, int flags)方法
    //通常进行重写
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //把数据写入Parcel
        dest.writeString(id);
        image.writeToParcel(dest,0);
        dest.writeString(text);
    }

    //自定义类型中必须含有一个名称为CREATOR的静态成员，该成员对象要求实现Parcelable.Creator接口及其方法
    public static final Parcelable.Creator<HistoryItem> CREATOR = new Parcelable.Creator<HistoryItem>() {
        @Override
        public HistoryItem createFromParcel(Parcel source) {
            //从Parcel中读取数据
            //此处read顺序依据write顺序
            HistoryItem item = new HistoryItem();
            //HistoryItem item = new HistoryItem(source.readString(), Bitmap.CREATOR.createFromParcel(source), source.readString());
            return item;
        }
        @Override
        public HistoryItem[] newArray(int size) {
            return new HistoryItem[size];
        }
    };
}
