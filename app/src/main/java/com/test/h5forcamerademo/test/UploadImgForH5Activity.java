package com.test.h5forcamerademo.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.test.h5forcamerademo.R;

import java.io.File;


public class UploadImgForH5Activity extends Activity {
    private WebView mWebView;
    private String url = "http://liyina91.github.io/liyina/UploadPictures/";
    private String photoPath;//拍照保存路径
    private final int REQUEST_CODE_TAKE_PHOTO = 1001;//拍照
    private final int REQUEST_FILE_PICKER     = 1002;//选择文件
    private static final int REQUEST_CODE_PERMISSION = 0x110;
    public ValueCallback mFilePathCallback;
    public ValueCallback mFilePathCallback4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_img_for_h5);
        initView();
        initData();
        initPermissionForCamera();
    }

    /**
     * Android 6.0以上版本，需求添加运行时权限申请；否则，可能程序崩溃111112223333444
     */
    private void initPermissionForCamera() {
        int flag = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (PackageManager.PERMISSION_GRANTED != flag) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE_PERMISSION == requestCode) {
            switch (grantResults[0]) {
                case PackageManager.PERMISSION_DENIED:
                    boolean isSecondRequest = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
                    if (isSecondRequest)
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
                    else
                        Toast.makeText(this, "拍照权限被禁用，请在权限管理修改", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void initData() {
        mWebView.loadUrl(url);
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings mSettings = mWebView.getSettings();
        mSettings.setJavaScriptEnabled(true);//开启javascript
        mSettings.setLoadWithOverviewMode(true);
        mSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//适应屏幕，内容将自动缩放

    }

    public class MyWebChromeClient extends WebChromeClient {
        //android 5.0+
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            goToTakePhoto();
            mFilePathCallback = filePathCallback;
            return true;
        }

        /**
         * 过时的方法：openFileChooser
         */
        public void openFileChooser(ValueCallback<Uri> filePathCallback) {
            openFileManager();
            mFilePathCallback4 = filePathCallback;
        }
        /**
         * 过时的方法：openFileChooser
         */
        public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
            goToTakePhoto();
            mFilePathCallback4 = filePathCallback;
        }
        /**
         * 过时的方法：openFileChooser
         */
        public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
            goToTakePhoto();
            mFilePathCallback4 = filePathCallback;
        }
    }

    /**
     * 调用系统相机
     */
    private void goToTakePhoto() {
       try {
           Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
           File photoFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
           photoFile = new File(photoFile, System.currentTimeMillis() + ".jpg");
           photoPath = photoFile.getAbsolutePath();
           if (Build.VERSION.SDK_INT > 23) {
               /**Android 7.0以上的方式**/
               Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
               grantUriPermission(getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
               intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
           } else {
               /**Android 7.0以下的方式**/
               intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
           }
           startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
       }catch (Exception e){
           int flag = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
           if (PackageManager.PERMISSION_GRANTED != flag) {
               Toast.makeText(this, "拍照权限被禁用，请在权限管理修改", Toast.LENGTH_SHORT).show();
           }

       }


    }

    /**
     * 打开文件管理器
     */
    public void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "选择文件"), REQUEST_FILE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理页面返回或取消选择结果
         */
        switch (requestCode) {
            case REQUEST_FILE_PICKER://文件管理器
                pickPhotoResult(resultCode, data);
                break;
            case REQUEST_CODE_TAKE_PHOTO://拍照
                takePhotoResult(resultCode);
                break;
            default:
                break;
        }
    }

    private void pickPhotoResult(int resultCode, Intent data) {
        if (mFilePathCallback != null) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result != null) {
                String path = getPath(this, result);
                Uri uri = Uri.fromFile(new File(path));
                mFilePathCallback.onReceiveValue(new Uri[]{uri});
                photoPath = path;
            } else {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }
            /**
             * 针对API 19之前的版本
             */
        } else if (mFilePathCallback4 != null) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result != null) {
                String path = getPath(this, result);
                Uri uri = Uri.fromFile(new File(path));
                mFilePathCallback4.onReceiveValue(uri);
            } else {
                mFilePathCallback4.onReceiveValue(null);
                mFilePathCallback4 = null;
            }
        }
    }

    private void takePhotoResult(int resultCode) {
        if (mFilePathCallback != null) {
            if (resultCode == RESULT_OK) {
                Uri uri = Uri.fromFile(new File(photoPath));
                mFilePathCallback.onReceiveValue(new Uri[]{uri});
            } else {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }
            /**
             * 针对API 19之前的版本
             */
        } else if (mFilePathCallback4 != null) {
            if (resultCode == RESULT_OK) {
                Uri uri = Uri.fromFile(new File(photoPath));
                mFilePathCallback4.onReceiveValue(uri);
            } else {
                mFilePathCallback4.onReceiveValue(null);
                mFilePathCallback4 = null;
            }
        }
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public  String getPath( Context context,  Uri uri) {
         boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                 String docId = DocumentsContract.getDocumentId(uri);
                 String[] split = docId.split(":");
                 String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                 String id = DocumentsContract.getDocumentId(uri);
                 Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                 String docId = DocumentsContract.getDocumentId(uri);
                 String[] split = docId.split(":");
                 String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                 String selection = "_id=?";
                 String[] selectionArgs = new String[]{split[1]};
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

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public  String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
         String column = "_data";
         String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                 int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public  boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public  boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public  boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
