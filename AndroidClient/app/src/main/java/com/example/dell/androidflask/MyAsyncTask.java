package com.example.dell.androidflask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAsyncTask extends AsyncTask<Void, Void, Bitmap>
{
    private String postUrl;
    private RequestBody postBody;
    private WeakReference<TextView> responseText;
    private WeakReference<ImageView> imageView;
    private Boolean result = false;

    // Constructor that provides a reference to the TextView from the MainActivity
    MyAsyncTask(String url, RequestBody pb, TextView tv, ImageView iv) {
        postUrl = url;
        postBody = pb;
        responseText = new WeakReference<>(tv);
        imageView = new WeakReference<>(iv);
    }

    @Override
    protected Bitmap doInBackground(Void... voids)
    {
        final Bitmap[] bitmap = {null};
//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

        Request request = new Request.Builder()
                .url(String.valueOf(postUrl))
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                result = false;
//                responseText.setText("Failed to Connect to Server");
                Log.e("TAG","下载失败onFailure="+e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
//                            responseText.setText(response.body().string());
                    InputStream in = response.body().byteStream();
                    bitmap[0] = BitmapFactory.decodeStream(in);
                    result = true;
//                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.e("TAG","下载失败onResPonse="+e);
                }
            }
        });
        return bitmap[0];
    }
    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        if(result == false){
            responseText.get().setText("Failed to Connect to Server");
        }else{
            imageView.get().setImageBitmap(bitmap);
        }
//        super.onPostExecute(result);
//        //    將doInBackground方法返回的byte[]解碼成要給Bitmap
//        Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
//        //    更新我們的ImageView控件
//        imageView.setImageBitmap(bitmap);
//        //    使ProgressDialog框消失
//        progressDialog.dismiss();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

}
