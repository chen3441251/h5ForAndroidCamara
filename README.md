直接调用camera拍照上传（兼容android7.0），统一不同版本机型相册与camera不统一，只测试了部分机型，测试demo仅供参考
有了js互调兼容了android4.4/4.4.1/4.4.2版本无法调用openFileChooser方法，需要后台配合，项目中的openCamera4K()是java调用android的相机，uploadImageCallBack（var）是android调用java上传照片
混淆规则：
-keepclassmembers class * extends android.webkit.WebChromeClient{
       public void openFileChooser(...);
       public boolean onShowFileChooser(...);
}
