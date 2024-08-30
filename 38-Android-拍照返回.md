
# Android 拍照返回笔记

## Demo 代码

[takephoto_sample](https://github.com/shuanghua/android-samples/tree/master/takephoto-sample/app/src/main/java/com/shuanghua/takephoto_sample)

# 拍照

## 打开系统相机

```kotlin
fun AppCompatActivity.takePictureFromCameraSample(takePictureLauncher: ActivityResultLauncher<Uri>) {
    val imgFile = File(externalCacheDir, "image_name.jpg")
    if (imgFile.exists()) {
        imgFile.delete()
    } else {
        try {
            imgFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    val imgUri = if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(imgFile) // file to uri
    } else { // FileProvider
        FileProvider.getUriForFile(
            this,
            // 这个字符串必须和清单文件中的 provider 的 authorities 一致, 只要一致随便改成什么都可以
            "$packageName.fileProvider", // android:authorities="${applicationId}.fileProvider"
            imgFile
        )
    }
    // val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    // intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri) // 这会获取拍照的原图 (如不需要原图则将该行注释掉)
    // startActivityForResult(intent, 100) // 启动相机拍照，(已经过时)
    takePictureLauncher.launch(imgUri) // 启动相机拍照，并将图片保存到 uri
}
```

## 处理拍好的图片

```kotlin
/**
 * 通过 registerForActivityResult 打开系统相机 (替代 startActivityForResult 过时的处理方式)
 */
private val takePictureLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicture()
) { result: Boolean -> // 接口回调，result 为拍照成功和失败
    if (result) { // imageUri 指向的文件已经有图片了
        //imageUri.let { imageView.setImageURI(it) }
        //val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        val bitmap = optionsImg(imgUri) // 先压缩图片，再显示
        imageView.setImageBitmap(bitmap)
    }
}
```

## 调用拍照方法

```kotlin
class MainActivityKotlin : AppCompatActivity() {

    private lateinit var imgUri: Uri

    private val takePictureLauncher = 
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean -> 
            if (result) { // 接口回调，result 为拍照成功和失败
                // imageUri.let { imageView.setImageURI(it) }
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                imageView.setImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button.setOnClickListener{
            takePictureFromCameraSample(takePictureLauncher)
        }
    }
}
```

完整代码：[takephoto_sample/MainActivityKotlin.kt](https://github.com/shuanghua/android-samples/blob/master/takephoto-sample/app/src/main/java/com/shuanghua/takephoto_sample/MainActivityKotlin.kt)

# 相册选择图片

## 打开系统相册

```kotlin
/**
 * 从相册选择图片并监听选择的结果
 */
fun pickPictureFromGallery(pickPictureLauncher: ActivityResultLauncher<PickVisualMediaRequest>) {
    //https://developer.android.com/training/data-storage/shared/photopicker?hl=zh-cn
    val mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
//    val mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly
//    val mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo
//    val mediaType = ActivityResultContracts.PickVisualMedia.SingleMimeType("image/jpeg") // image/jpeg, image/png, image/gif, image/webp ,image/*
    pickPictureLauncher.launch(PickVisualMediaRequest(mediaType)) // 启动相册选择，图片请在 pickPictureLauncher 的回调中获取
}
```

## 处理选择的图片

```kotlin
private val pickPictureLauncher = registerForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { imgUri: Uri? ->
    imgUri?.let { imageView.setImageURI(it) }
}
```

# AndroidManifest

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
            android:authorities="${applicationId}.fileProvider"
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


