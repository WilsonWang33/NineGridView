package com.lzy.ninegrid.preview;

/**
 * Created by Administrator on 2016/8/17.
 */

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * 图片下载的工具类
 */
public  class DownPicUtil {

    /**
     *下载图片，返回图片的地址
     * @param url
     */
    public static void downPic(String url, DownFinishListener downFinishListener){
        // 获取存储卡的目录
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath+ File.separator+"Zyh");
        if(!file.exists()){
            file.mkdir();
        }

        if(url.indexOf("data:image")==-1) {
            loadPic(file.getPath(), url, downFinishListener);
        }else{
            url = url.replace("data:image/png;base64,","");
            url = url.replace("data:image/jpeg;base64,","");
            url = url.replace("data:image/jpg;base64,","");
            loadPicBase64(file.getPath(), url, downFinishListener);
        }

    }

    private static void loadPic(final String filePath, final String url, final DownFinishListener downFinishListener) {
        Log.e("下载图片的url",url);
        new AsyncTask<Void,Void,String>(){
            String fileName;
            InputStream is;
            OutputStream out;

            @Override
            protected String doInBackground(Void... voids) {

                // 下载文件的名称
//                String[] split = url.split("/");
                 fileName = System.currentTimeMillis()+".jpg";
                // 创建目标文件,不是文件夹
                File picFile = new File(filePath + File.separator + fileName);
                if(picFile.exists()){
                    return  picFile.getPath();
                }

                try {
                    URL picUrl = new URL(url);
                    //通过图片的链接打开输入流
                    is = picUrl.openStream();
                    if(is==null){
                        return null;
                    }
                    out = new FileOutputStream(picFile);
                    byte[] b=new byte[1024];
                    int end ;
                    while ((end=is.read(b))!=-1){
                        out.write(b,0,end);
                    }

                    if(is!=null){
                        is.close();
                    }

                    if(out!=null){
                        out.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



                return picFile.getPath();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s!=null){
                    downFinishListener.getDownPath(s);
                }
            }
        }.execute();
    }
    private static void loadPicBase64(final String filePath, final String url, final DownFinishListener downFinishListener) {
        Log.e("下载图片的url",url);
        new AsyncTask<Void,Void,String>(){
            String fileName;

            @Override
            protected String doInBackground(Void... voids) {

                // 下载文件的名称
//                String[] split = url.split("/");
                fileName = System.currentTimeMillis()+".jpg";
                // 创建目标文件,不是文件夹
                File picFile = new File(filePath + File.separator + fileName);
                if(picFile.exists()){
                    return  picFile.getPath();
                }
                try {
                    //byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
                    byte[] buffer = Base64.decode(url, Base64.DEFAULT);
                    FileOutputStream out = new FileOutputStream(picFile);
                    out.write(buffer);
                    out.close();
                }catch (Exception e){

                }
                return picFile.getPath();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s!=null){
                    downFinishListener.getDownPath(s);
                }
            }
        }.execute();
    }
    //下载完成回调的接口
    public interface DownFinishListener{

        void getDownPath(String s);
    }
}
