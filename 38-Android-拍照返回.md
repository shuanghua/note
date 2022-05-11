---
toc: false
comments: true
title: Android 拍照返回-笔记
description: Android 拍照返回-笔记
tags:
  - Android
id: 38
categories:
  - Android
date: 2018-6-26
---

---
Android 拍照存储笔记

<!-- more -->


# 拍照
```java
    private File imgFile;
	private String path;// 注意当前路径，为啥？下面讲到。

    public void takePhotoBy8(int requestCode) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";//文件的名字
        imgFile = new File(path, fileName);//生成该文件的 File 对象
        Uri imgUri;

        if (Build.VERSION.SDK_INT < 24) {
            imgUri = Uri.fromFile(imgFile);
        } else {//Android 7.0 访问本地文件的方式有变化，变得更加安全。
            // 注意：必须在 AndroidManifest.xml 添加对应的 provider，
            // 这里的第二个参数必须和 AndroidManifest 中 provider里面的 authorities: = 一致
            imgUri = FileProvider.getUriForFile(this, getPackageName(), imgFile);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, requestCode);
    }
```

# 返回拍好的图片
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(path + fileName);
            //处理你的图片
        }
    }
```

# AndroidManifest.xml
```xml
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <!--适配7.0+拍照返回 Start-->
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_path" />
        </provider>
        <!--适配7.0+拍照返回 End-->
				
    </application>
```
> authorities 的值必须和  FileProvider.getUriForFile(this, getPackageName(), imgFile);的第二个参数一致，这里的第二个参数是  getPackageName()，不一定是包名，可以写别的字符串，但必须和清单文件一致。


# xml/provider_path.xml
在 res 目录下创建 xml 目录，然后在创建好的 xml 目录下再创建一个 xml 文件，名字随意。这里是 provider_path.xml

```xml
<paths>
    <external-path
        name="external_files"
        path="." />
    <root-path
        name="external_files"
        path="/storage/"/>
</paths>
```
该 xml 文件指明自己的 App 的共享目录，paths 标签下可选的 子标签有：

- **external-path** 对应的目录是：Environment.getExternalStorageDirectory() = /storage/emulated/0/
- **files-path** 对应的目录是： Context.getFilesDir()
- **cache-path** 对应的目录是： Context.getCacheDir()
- **external-files-path** 对应的目录是： Context.getFilesDir()
- **external-cache-path** 对应的目录是：Context.getExternalCacheDir()
- **root-path** 设备的根目录 对应 new File("/")

所以说 图片的存储位置，决定了要选用的哪个 path

- **name**: path目录的别名，设置一个别名来替换真实的路径，在 content uri中显示的是别名
- **path**: 目录的真实路径

####  name 和 path 的例子

> 假设保存图片的完整路径是：**/storage/emulated/0/MyApp/image/风景.jpg**

把完整路径分成三部分：

- /storage/emulated/0
- /MyApp/image
- 风景.jpg

其中 external-path 对应的是 /storage/emulated/0 这段路径
真实路径 path 对应的是 /MyApp/image，
然后再给 path 设置一个别名，避免对外暴露真实路径，这也就是我们设置给 name 的值

那么 provider_path.xml 应该是这样的：
```xml
<paths>
    <external-path
        name="BieMing"
        path="MyApp/image" />
</paths>
```
生成的 content:uri就是： 
`
content://com.xxx.demo/BieMing/image/风景.jpg
`

---

上面这段代码能最完全的隐藏掉我们的真实路径。当然也可以不完全，比如下面这样隐藏替换其中的一部分也是可以的：
```xml
<paths>
    <external-path
        name="BieMing"
        path="MyApp" />
</paths>
```
生成的 content:uri就是： 
`
content://com.xxx.demo/BieMing/image/风景.jpg
`
> 其中 com.xxx.demo 就是我们在 AndroidManifest.xml 中 authorities 的值。当 path = "." 时，点号代指的时根目录，也就是external-path


#### Demo 代码：[https://gitee.com/shua17/AndroidTakePhotoReturn](https://gitee.com/shua17/AndroidTakePhotoReturn)