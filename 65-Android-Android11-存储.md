# 说明

早期的 Android 手机是带有 sdCard 插槽， 而访问 sdCard 目录是通过 getExternalFileDir() 来访问， 访问内部存储目录则是通过 getFileDir 来访问；在现在的新手机 sdCard 直接内置到手机存储中了，所以现在很少需要判断 sdCard 是否存在的情况， getFileDir 应用内部存储是占用应用内存空间的，只适合存储需要隐私的小文件。

1. 大文件和不需要隐私的的文件存储在 getExternalFileDir()
2. 小文件且需要隐私的文件存储在 getFileDir()

从上面的介绍来看，缺点很明显，假设用户需要存储大并且是隐私文件只能自己添加一层加密然后保存到 getExternalFileDir()
这时候一部手机在安装了各种公司的 app 之后,  外部存储目录下就有各种乱七八糟的目录。

现在到 Android 11 , 谷歌开始要解决这个问题了：

1. getExternalFileDir() 现在变成应用私有外部目录，A 应用将不能像过去的请求读写权限来访问 B 应用的 getExternalFileDir() (但还可以通过权限授权来访问，一般文件管理器才用到)
2. getFileDir() 和以前一样是应用的私有内部目录，自己应用访问不需要任何权限，系统会自动加密该目录，别的应用除在 root 系统下给任何权限也都不能访问。
3. 限制在 getExternalFileDir() 之外的地方随意建立目录
4. 需要共享的文件只能放到特定的共享目录下，Environment.getExternalStoragePublicDirectory； 例如 DCIM Pictures Movies Downloads （强烈建议存到这些目录的时候另外建立一个以自己应用名为目录）

### Android 5.0 --> Android 9.0

不管读写取外部存储的哪个文件夹都需要这两个权限

# Android 10 +

> 以下内容只针对 Android 10+ 的 【分区存储】

## 共享目录:

- DCIM 
- Pictures
- Movies
- Downloads

> Environment.DIRECTORY_PICTURES + "/" + "AppName"

例如用户需要保存一张图片，希望这张图片能永久保存（无视应用卸载），同时还能再系统相册中显示，那么请选择 [共享目录] 保存。

#### 共享目录权限

1. 默认情况下，自己应用读写共享目录下自己的文件不需要任何权限
2. 自己应用卸载重装后，访问共享目录过去的文件需要读权限
3. 自己应用访问共享目录下别的应用保存的文件，需要读权限，并且不能修改别人的文件，你只能读取后写入成自己的文件特征（也就是属于自己的文件）

当你需要兼容Android 10 之前的代码，你需要添加 android:maxSdkVersion="28" 来限定写的权限只能在之前的版本有效

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

> 在 Android 10 之前，我们可以在获得读写权限后，利用 File 肆无忌惮的操作文件， 在 Android 10 + 之后，File 的功能废了一半，官方建议使用  MediaStore + ContentResolver + Uri 的方式进读写。

#### 保存图片到共享目录

```java
/**
 * 保存到 Pictures 目录下
 *
 * @param context   activity
 * @param imageName 图片的名字
 * @param suffix    图片的格式：jpg 或者 png
 * @param directory 图片的目录（只需要层一目录，一般写自己的应用名字）
 * @param mimeType  传 image/jpeg 或 mime/png
 * @param bitmap    bitmap 资源
 * @return 返回保存成功后的图片对应数据库表的 Uri,
 */
public static Uri saveImage(
        Activity activity, 
        String imageName,
        String suffix,
        String directory,
        String mimeType,
        Bitmap bitmap
    ) throws IOException {

    final String imageDir = Environment.DIRECTORY_PICTURES + File.separator + directory;

    ContentResolver resolver = activity.getApplicationContext().getContentResolver();
    ContentValues values = new ContentValues();

    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);

    // 设置存放目录
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {// Android 10 之后， 无需通知相册更新
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, imageDir);
    } else {                                             // 需要通知相册更新
        File dirFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), directory);
        if (!dirFile.exists()) dirFile.mkdirs();// 必须先创建第一层目录: 应用名为目录
        File imageFile = File.createTempFile(imageName, suffix, dirFile);//然后创建全目录文件
        values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
    }
    // 这个Uri可直接用于分享(不用使用 fileProvider)
    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    assert uri != null;
    OutputStream outputStream = resolver.openOutputStream(uri);// 获取写出流
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);// 写入
    assert outputStream != null;
    outputStream.flush();
    outputStream.close();
    return uri;
}
```

#### 访问共享目录的图片

```java
/**
 * 获取 Pictures 目录下某一张图片
 *
 * @param Activity   activity
 * @param imageName 图片的名字
 * @return 可根据需要返回 Bitmap 或者 Uri,如果需要获取多张图片，只需要把Urii缓存到 List 中后再统一转换成 bitmap
 */
public Bitmap getImage(Activity activity, String imageName) throws IOException {
    Uri uri = null;
    // 需要的信息，例如图片的所在数据库表对应的 ID，图片在数据库中的名字,这些属于 ContentResolver Cursor 的基本使用方式
    String[] projection = {
            MediaStore.Images.Media._ID, // id 用于生成 uri ，所以是必须要获取的
            MediaStore.Images.Media.DISPLAY_NAME // 图片的名字，用于获取具体的图片
    };
    String selection = MediaStore.Images.Media.DISPLAY_NAME + " == ?";
    String[] selectionArgs = new String[]{imageName};
    Context context = activity.getApplicationContext();
    ContentResolver resolver = context.getContentResolver();

    try (Cursor cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
    )) {
        assert cursor != null;
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);
            uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        }
    }
    if (uri != null) {
        InputStream inputStream = resolver.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }
    return null;  //没有找到该图片
}
```

上面的代码是将图片存到共享目录中，File 的身影也仅在保存图片的方法中的出现了一次， 而且还是在小于 Android 10 的范围内使用到。后面的读取图片不需要 Api 判断， File 的影子都看不到一个。

当然，File 在操作私有目录的时候还是要用到的

> 读取和写入的操作属于 IO 操作，应尽量放在 io 线程中执行。