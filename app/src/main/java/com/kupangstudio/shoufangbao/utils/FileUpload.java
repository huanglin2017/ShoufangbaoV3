package com.kupangstudio.shoufangbao.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by long1 on 15/11/27.
 * Copyright 15/11/27 android_xiaobai.
 */
public class FileUpload extends AsyncTask{
    String actionUrl;
    Map<String, String> params;
    Map<String, File> files;
    UploadListener listener;
    ProgressDialog dialog;

    public interface UploadListener {
        public void onUploadEnd(boolean success, String object);
    }

    public FileUpload(String actionUrl, Map<String, String> params, Map<String, File> files, UploadListener listener, ProgressDialog dialog) {
        this.actionUrl = actionUrl;
        this.params = params;
        this.files = files;
        this.listener = listener;
        this.dialog = dialog;
    }

    @Override
    protected String doInBackground(Object... arg0) {
        String reslut = null;
        try {
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            String PREFIX = "--", LINEND = "\r\n";
            String MULTIPART_FROM_DATA = "multipart/form-data";
            String CHARSET = "UTF-8";
            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);
                sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
                sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sb.append(LINEND);
                sb.append(entry.getValue());
                sb.append(LINEND);
            }
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes());

            // 发送文件数据
            if (files != null)
                for (String key : files.keySet()) {
                    StringBuilder sb1 = new StringBuilder();
                    File valueFile = files.get(key);
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data;name=\""+ key+"\";filename=\""+ valueFile.getName() + "\"\r\n");
                    sb1.append("Content-Type: multipart/form-data; charset=" + CHARSET + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());
                    InputStream is = new FileInputStream(valueFile);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }
                    is.close();
                    outStream.write(LINEND.getBytes());
                }
            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            InputStream in = conn.getInputStream();
            InputStreamReader isReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String line = null;
            reslut = "";
            while ((line = bufReader.readLine()) != null)
                reslut += line;
            outStream.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reslut;
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected void onCancelled() {
        //取消操作
        if (listener != null) {
            listener.onUploadEnd(false, null);
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (listener != null) {
            if (result == null) {
                listener.onUploadEnd(false, "");
            } else {
                listener.onUploadEnd(true, result.toString());
            }
        }
    }


    @Override
    protected void onProgressUpdate(Object... values) {

    }


}
