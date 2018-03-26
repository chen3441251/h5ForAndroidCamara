package com.test.h5forcamerademo.test;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.test.h5forcamerademo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;


public class UploadImgForH5Activity extends Activity {
    private WebView mWebView;
        private String url = "http://liyina91.github.io/liyina/UploadPictures/";
    private String photoPath;//拍照保存路径
    private final int REQUEST_CODE_TAKE_PHOTO       = 1001;//拍照
    private final int PERMISSION_REQUESTCODE_CAMERA = 1002;//选择文件
    public  ValueCallback mFilePathCallback;
    private Uri           mContentUri;
    private File          mPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_img_for_h5);
        initView();
        initData();
    }

    private void initData() {
        mWebView.loadUrl(url);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());

    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings mSettings = mWebView.getSettings();
        mSettings.setJavaScriptEnabled(true);//开启javascript
        mSettings.setDomStorageEnabled(true);//开启DOM
        mWebView.addJavascriptInterface(new JsInterface(), "Android");
    }

    /**
     * 支持android4.4/4.4.1/4.4.2版本
     */
    private String photoType;

    private class JsInterface {

        @JavascriptInterface
        /**
         * type: front身份证正面  back身份证背面 similarity_person手持
         * */
        public void openCamera4K() {
            goToTakePhoto();
        }
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
            goToTakePhoto();
            mFilePathCallback = filePathCallback;
        }

        /**
         * 过时的方法：openFileChooser
         */
        public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
            goToTakePhoto();
            mFilePathCallback = filePathCallback;
        }

        /**
         * 过时的方法：openFileChooser
         */
        public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
            goToTakePhoto();
            mFilePathCallback = filePathCallback;
        }
    }

    /**
     * 调用系统相机
     */
    private void goToTakePhoto() {
        checkPermission();

    }

    private void checkPermission() {
        boolean bcamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
        //如果camera或者读取权限没有授权
        if (Build.VERSION.SDK_INT >= 23 && (bcamera)) {
            //用户上一次拒绝后这一次提醒用户为什么需要这个权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showPermissionTips("只有开启camera权限才能拍照");
            }  else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUESTCODE_CAMERA);

            }
        } else {
            //已经授权，直接调用相机
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTCODE_CAMERA:
                if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    openCamera();
                } else {
                    //如果拒绝勾选了不再提示
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                        //权限已经被拒绝不在提示了
                        startAppSettings("不开启camera权限就无法使用拍照,请去应用权限管理开启权限");
                    }else {
                        Toast.makeText(this, "请授权开启相机权限", Toast.LENGTH_SHORT).show();
                    }
                    if (mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                        mFilePathCallback = null;
                    }
                }
                break;

        }


    }

    //应用设置界面
    private void startAppSettings(String message) {
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .show();

    }

    //应用设置界面
    private void showPermissionTips(String message) {
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(UploadImgForH5Activity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUESTCODE_CAMERA);
                    }
                })
                .show();

    }

    /**
     * 判断系统中是否存在可以启动的相机应用
     *
     * @return 存在返回true，不存在返回false
     */
    public boolean hasCamera() {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void openCamera() {
        if (!hasCamera()) {
            Toast.makeText(this, "没有可用相机，请检查", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPhotoFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
            try {
                if (mPhotoFile.exists()) {
                    mPhotoFile.delete();
                }
                mPhotoFile.createNewFile();
            } catch (Exception e) {

            }
            photoPath = mPhotoFile.getAbsolutePath();
            if (Build.VERSION.SDK_INT > 23) {
                /**Android 7.0以上的方式**/
                mContentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", mPhotoFile);
                grantUriPermission(getPackageName(), mContentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                mContentUri = Uri.fromFile(mPhotoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mContentUri);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        } catch (Exception e) {
            int flag = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (PackageManager.PERMISSION_GRANTED != flag) {
                Toast.makeText(this, "拍照权限被禁用，请在权限管理修改", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }
            return;
        }
        /**
         * 处理页面返回或取消选择结果
         */
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO://拍照
                takePhotoResult();
                break;
            default:
                break;
        }
    }


    private void takePhotoResult() {
        if (mFilePathCallback != null) {
            try {
                compressAndSaveBitmap(mPhotoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT > 19) {
                mFilePathCallback.onReceiveValue(new Uri[]{mContentUri});
            } else {
                mFilePathCallback.onReceiveValue(mContentUri);
            }

        } else {
            /**
             * js交互支持android4.4/4.4.1/4.4.2等版本
             * */
            Bitmap bitmap = null;
            try {
                bitmap = compressAndSaveBitmap(mPhotoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final Bitmap finalBitmap = bitmap;
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:uploadImageCallBack('" + bitmaptoString(finalBitmap) + "')");
                }
            });
        }

    }

    public static String bitmaptoString(Bitmap bitmap) {
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.NO_WRAP);
        return string;
    }

    /**
     * 通过uri获取图片并进行压缩
     *
     * @param file
     */
    public Bitmap compressAndSaveBitmap(File file) throws IOException {
        Uri uriFromFile = transCameraFile2Uri(file);
        InputStream input = getApplicationContext().getContentResolver().openInputStream(uriFromFile);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        onlyBoundsOptions.inSampleSize = calculateInSampleSize(onlyBoundsOptions, 480, 800);
        //设置缩放比例
        onlyBoundsOptions.inJustDecodeBounds = false;
        input = getApplicationContext().getContentResolver().openInputStream(uriFromFile);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        return compressImage(bitmap, file);//再进行质量压缩
    }

    //拍照定义的File存储路径转换成uri
    private Uri transCameraFile2Uri(File photoFile) {
        Uri photoUri = null;
        if (Build.VERSION.SDK_INT > M) {
            //android7.0
            photoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            grantUriPermission(getApplicationContext().getPackageName(), photoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            photoUri = Uri.fromFile(photoFile);
        }
        return photoUri;
    }

    // 计算 BitmapFactpry 的 inSimpleSize的值的方法
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqWidth == 0 || reqHeight == 0) {
            return 1;
        }
        // 获取图片原生的宽和高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        // 如果原生的宽高大于请求的宽高,那么将原生的宽和高都置为原来的一半
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public Bitmap compressImage(Bitmap image, File file) {
        //读取图片角度，纠正角度（针对三星手机拍照会旋转）
        int bitmapDegree = getBitmapDegree(file.getPath());
        image = rotateBitmapByDegree(image, bitmapDegree);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(baos.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        return bitmap;
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;

    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


}
