直接调用camera拍照上传（兼容android7.0），统一不同版本机型相册与camera不统一，只测试了部分机型，测试demo仅供参考
混淆规则：
-keepclassmembers class * extends android.webkit.WebChromeClient{
       public void openFileChooser(...);
       public boolean onShowFileChooser(...);
}
