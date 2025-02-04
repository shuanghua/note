# 说明

早期的 Android 手机是带有 sdCard 插槽， 而访问 sdCard 目录是通过 getExternalFileDir() 来访问， 访问内部存储目录则是通过 getFileDir 来访问； 现在的新手机 sdCard 相当于直接内置到手机存储中了， 所以现在很少需要判断 sdCard 是否存在的情况， getFileDir 内部存储是占用应用内存空间的， 只适合存储需要隐私的小文件。

通常：

1. 大文件和不需要隐私的的文件存储在 getExternalFileDir() （应用外部存储专属目录）
2. 小文件且需要隐私的文件存储在 getFileDir() （应用内部存储专属目录）
3. 公开文件（例如图片、视频、音频）存储在 Environment.getExternalStoragePublicDirectory() （外部存储共享目录）

> getExternalStoragePublicDirectory() 在 Android 10 + 中被限制， 只能使用 MediaStore + ContentResolver + Uri 的方式访问。

从上面的介绍来看， 有一个缺点， 假设用户需要存储一份大并且是隐私文件只能自己添加一层加密然后保存到 getExternalFileDir()
没有统一的目录和命名， 这时候一部手机在安装了各种公司的 app 之后, 外部存储目录下就有各种乱七八糟的目录。

现在到 Android 11 , 谷歌开始要解决这个问题了：

1. getExternalFileDir() 现在变成应用私有外部目录（应用外部存储专属目录）， A 应用将不能像过去一样直接通过 File 对象操作别的应用的外部存储目录（getExternalFileDir）， 就算是申请了写权限也不行 (当然文件管理器一类的 App 还是可以通过申请： MANAGE_EXTERNAL_STORAGE 权限来访问的， 但如果不是文件管理器 App, 却申请了 MANAGE_EXTERNAL_STORAGE 权限，则不能通过谷歌商店审核)
2. getFileDir() 和以前一样是应用的私有内部目录（应用内部存储专属目录），自己应用访问不需要任何权限， 在  Android 10（API 级别 29）及更高版本中， 系统会自动加密该目录， root 权限 就算可以访问， 也得想办法处理解密才行。
3. 限制在 getExternalFileDir() 之外的地方随意建立目录
4. 需要共享的文件只能放到特定的共享目录下，Environment.getExternalStoragePublicDirectory； 例如 DCIM Pictures Movies Downloads （强烈建议存使用到这些目录的时候， 另外建立一个以自己应用名为目录， 然后存放文件）

### Android 5.0 --> Android 9.0

通过 File 类操作外部存储目录， 需要申请读写权限

# Android 10 +

【分区存储】+ MediaStore + ContentResolver + Uri

以下内容只针对 Android 10+

## 共享目录

外部存储共享目录 （相当于之前的 getExternalStoragePublicDirectory()）

- DCIM
- Pictures
- Movies
- Downloads

> Environment.DIRECTORY_PICTURES + "/" + "AppName"

例如用户需要保存一张图片，希望这张图片能永久保存（无视应用卸载）， 同时还能在系统相册中显示，那么请选择 [共享目录] 保存。
希望卸载应用，图片也被删除，那么请选择 [外部存储的应用私有目录] 保存。
对于小的文件，需要考虑安全，请存储到[内部存储的应用私有目录]，注意内部存储会占用应用的运行内存，请谨慎使用。[内部存储的应用私有目录]的读写也更加高效

### 共享目录权限

1. 默认情况下，自己应用读写共享目录下自己的文件是不需要任何权限
2. 自己应用卸载重装后，访问共享目录过去的文件需要读权限
3. 自己应用访问共享目录下别的应用保存的文件，需要读权限，并且不能修改别人的文件，你只能读取后写入成自己的文件特征（也就是属于自己的文件）

> 从上面的第 3 点可以得出结论， 只对于 Android 10 + 其实只要读权限就行，不需要写权限， 当然为了兼容之前的版本， 才需要在清单文件中添加读写权限。

当你需要兼容 Android 10 之前的代码， 你需要添加 android:maxSdkVersion="28" 来限定写的权限只能在之前的版本有效

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28"
/>
```

## 私有目录

- context.getExternalFilesDir(DIRECTORY_PICTURES)
- context.getFilesDir()
- context.getExternalMediaDirs()

私有目录下的文件只对自己的应用可见，别的应用无法访问，就连系统相册也无法扫描加载这个目录里面的图片。
私有目录在自己的应用被卸载后会被删除

#### 私有目录权限

调用 getExternalFilesDir() 和 getFilesDir() 不需要 任何读写权限

# 访问共享目录代码

> 在 Android 10 之前，我们可以在获得读写权限后，利用 File 肆无忌惮的操作文件， 在 Android 10 + 之后，File 的作用废了一半，官方建议使用  MediaStore + ContentResolver + Uri 的方式进读写。

## 保存图片到共享目录

```kotlin
/**
 * 保存到共享目录 Pictures 下 （已适配全版本（
 *
 *  Android Q + 使用 MediaStore
 *  Android Q 之前，请申请权限后使用 File
 *
 * sdcard/Pictures/图片名.后缀
 *
 * sdcard/Pictures/应用名/图片名.后缀
 * @param context   activity
 * @param imageName 图片的名字
 * @param suffix    图片的格式：jpg 或者 png
 * @param directory 图片的目录（只需要层一目录，强烈建议写自己应用的名字）
 * @param mimeType  传 image/jpeg 或 mime/png
 * @param bitmap    bitmap 资源
 * @return 保存成功后的图片路径 Uri
 */
@Throws(IOException::class)
fun saveImageToSharedPictures(
    context: Activity,
    imageName: String,
    suffix: String?,
    directory: String = context.getString(R.string.app_name),
    mimeType: String?,
    bitmap: Bitmap
): Uri {
    // 如果要保存到其它公享目录，可以修改这里的 DIRECTORY_PICTURES
    val shareDir = Environment.DIRECTORY_PICTURES
    val imageDir = shareDir + File.separator + directory

    val resolver = context.applicationContext.contentResolver
    val values = ContentValues()

    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //  Q = api 29
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, imageDir) // MediaStore 方式
    } else { // Q 之前的需要申请读和写的权限
        val dirFile = File(Environment.getExternalStoragePublicDirectory(shareDir), directory)
        if (!dirFile.exists()) {
            dirFile.mkdirs() // 必须先创建第一层目录
        }
        val imageFile = File.createTempFile(imageName, suffix, dirFile) //然后创建全目录
        values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
    }

    val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
    val outputStream = resolver.openOutputStream(uri) //获取写出流
    outputStream?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) } // 写入 bitmap
    return uri //这个Uri可直接用于分享(不用使用 fileProvider)
}
```

## 访问共享目录的图片

```kotlin
/**
 * 根据图片名字获取图片
 * 同时建议申请读取权限（Q+ 也建议，因为 Q+ 在应用卸载重装的情况下会把上次的图片识别为非自己目录的文件）
 * 请在 io 线程调用
 *
 * @param imageName 图片名字，[ 一定要包含后缀格式 ]
 * @return 图片的 Bitmap 形式
 */
@Throws(IOException::class)
fun getImageFromSharedPictures(activity: Activity, imageName: String): Bitmap? {
    var uri: Uri? = null

    val projection = arrayOf( // 需要的信息，例如图片的所在数据库表对应的 ID，图片在数据库中的名字
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )
    val selection = MediaStore.Images.Media.DISPLAY_NAME + " == ?"
    val selectionArgs = arrayOf(imageName)

    val context = activity.applicationContext
    val resolver = context.contentResolver

    resolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    ).use { cursor ->
        checkNotNull(cursor)
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
            )
        }
    }
    if (uri != null) {
        val inputStream = resolver.openInputStream(uri!!)
        return BitmapFactory.decodeStream(inputStream)
    }
    return null
}
```

上面的代码是将图片存到共享目录 Picktures 中， File 的身影也仅在保存图片的方法中的出现了一次， 而且还是在小于 Android 10 的范围内使用到。 后面的读取图片不需要 Api 判断， File 的影子都看不到一个。

当然， File 在操作专属目录的时候还是要用到的

> 读取和写入的操作属于 IO 操作，应放在 io 线程中执行

```
Environment.getExternalStoragePublicDirectory()  // 外部存储的 公共目录
context.getExternalFilesDir(DIRECTORY_DOWNLOADS) // 外部存储的 私有目录
context.getFileDir() // 内部存储的私有目录
```


# 保存 zip 文件
```kotlin
/**
 * sdcard/Download/zip文件名.zip
 *
 * sdcard/Download/应用名/zip文件名.zip
 * @param zipFileName 文件的名字+格式
 * @param directory 文件的目录（只需要一层目录，建议写自己应用的名字）
 * @param mimeType  如 application/zip
 * @param byteArray 文件的字节数组
 * @return 保存成功后的zip文件路径 Uri
 */
fun saveZipFileToSharedDownloads(
    context: Context,
    zipFileName: String,
    directory: String = context.getString(R.string.app_name),
    mimeType: String?,
    byteArray: ByteArray
): Uri {
    // 如果要保存到其它共享目录，可以修改这里的 DIRECTORY_DOWNLOADS
    val shareDir = DIRECTORY_DOWNLOADS
    val zipDir = shareDir + File.separator + directory

    val resolver = context.applicationContext.contentResolver
    val values = ContentValues()

    values.put(MIME_TYPE, mimeType)
    values.put(DISPLAY_NAME, zipFileName)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // MediaStore 方式 (需要大于或等于 AndroidQ = api29 = Android10)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, imageDir) // MediaStore 方式
    } else { // Q 之前的需要申请读和写的权限
        val dirFile = File(Environment.getExternalStoragePublicDirectory(shareDir), directory)
        if (!dirFile.exists()) {
            dirFile.mkdirs() // 必须先创建第一层目录
        }
        val imageFile = File.createTempFile(imageName, suffix, dirFile) //然后创建全目录
        values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
    }


    // 注意这里是 MediaStore.Downloads.EXTERNAL_CONTENT_URI
    val uri = checkNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
    val outputStream = resolver.openOutputStream(uri) // 获取写出流
    outputStream?.use { it.write(byteArray) } // 写入 byte array
    return uri
}
```


# 打印可用空间
```kotlin
val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
val externalSpace = getAvailableSpace(externalFilesDir)
Log.d("", "外部存储私有目录可用空间：${formatSize(externalSpace)}")
```

```kotlin
fun getAvailableSpace(dir: File?): Long {
    return if (dir != null && dir.exists()) {
        val statFs = StatFs(dir.path)
        val availableBlocks = statFs.availableBlocks.toLong()
        val blockSize = statFs.blockSize.toLong()
        availableBlocks * blockSize
    } else {
        0L
    }
}
fun formatSize(size: Long): String {
    val kb = 1024L
    val mb = kb * 1024L
    val gb = mb * 1024L
    return when {
        size >= gb -> String.format("%.2f GB", size.toDouble() / gb)
        size >= mb -> String.format("%.2f MB", size.toDouble() / mb)
        size >= kb -> String.format("%.2f KB", size.toDouble() / kb)
        else -> "$size B"
    }
}
```