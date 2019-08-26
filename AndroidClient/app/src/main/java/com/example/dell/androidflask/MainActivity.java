package com.example.dell.androidflask;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String selectedImagePath;
    String selectedImagePath2;
    private  TextView textView;
    private ImageView imageView;
    private ImageView imageView2;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int DOWNLOAD_FAILED = 0, DOWNLOAD_SUCCESS = 1;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.responseText);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        handler=new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DOWNLOAD_FAILED:
//                        Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                        break;
                    case DOWNLOAD_SUCCESS:
//                        Toast.makeText(this, "下载成功", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = (Bitmap) msg.obj;
                        imageView.setImageBitmap(bitmap);
                        textView.setText("Got Response");
//                        activity.imageView.setVisibility(View.VISIBLE);
//                        activity.imageView.setImageBitmap(bitmap);
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public void connectServer(View v){
//        EditText ipv4AddressView = findViewById(R.id.IPAddress);
//        String ipv4Address = ipv4AddressView.getText().toString();
//        EditText portNumberView = findViewById(R.id.portNumber);
//        String portNumber = portNumberView.getText().toString();

//        String postUrl= "http://"+ipv4Address+":"+portNumber+"/";
        String postUrl= "http://140.116.245.103:5001/";

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        options.inJustDecodeBounds = false;
        // Read BitMap by file path
//        File file = new File(selectedImagePath);

        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
        if(bitmap == null)
            Log.e("TAG","null "+selectedImagePath);
//        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        //style begin
        Bitmap bitmap2 = BitmapFactory.decodeFile(selectedImagePath2, options);
        if(bitmap2 == null)
            Log.e("TAG","style null "+selectedImagePath2);
//        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
        byte[] byteArray2 = stream2.toByteArray();
        //style end

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("content", "content.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .addFormDataPart("style", "style.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray2))
                .build();

        TextView responseText = findViewById(R.id.responseText);
        responseText.setText("Please wait ...");


        //move to async
//        new MyAsyncTask(postUrl, postBodyImage, textView, imageView).execute();

        //original method
        postRequest(postUrl, postBodyImage);
    }

    public String getPath(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        return path;
    }


    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("Failed to Connect to Server");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                InputStream in = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                Message msg = Message.obtain();
                msg.obj = bitmap;
                msg.what = bitmap==null?DOWNLOAD_FAILED:DOWNLOAD_SUCCESS;

                handler.sendMessage(msg);
//                imageView.setImageBitmap(bitmap);
//                textView.setText("Got Response");
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        TextView responseText = findViewById(R.id.responseText);
//                        try {
////                            responseText.setText(response.body().string());
//                            InputStream in = response.body().byteStream();
//                            Bitmap bitmap = BitmapFactory.decodeStream(in);
//                            imageView.setImageBitmap(bitmap);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                 });
            }
        });
    }

    public void selectImage(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }


    public void selectImage2(View view) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                // do your stuff..
                if(reqCode == 0) {
                    imageView.setImageURI(uri);
                    selectedImagePath = getPath(getApplicationContext(), uri);
                }else{
                    imageView2.setImageURI(uri);
                    selectedImagePath2 = getPath(getApplicationContext(), uri);
                }
//                EditText imgPath = findViewById(R.id.imgPath);
//                imgPath.setText(selectedImagePath);
//                Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}

