# Preferences DataStore 和 Proto DataStrore
- Preferences 主要以键值对的形式存储，类型安全不确定
- Proto 将数据作为自定义数据类型的实例进行磁盘存储，需要写一个定义架构（协议缓冲区），类型安全


# Proto DataStrore
根据 Proto 的介绍中“实例”一词，就不难猜出，Proto 需要一个序列化的操作
首先按序列化的步骤，通常需要定义一个数据对象(类似 Java Bean)，然后序列化它，而在 Proto DataStrore 中，定义这个数据对象需要根据 protobuf 语法去定义

1. 首先在 app/src/main/ 下创建一个 proto 包名 ，然后在 app/src/main/proto/ 包下创建一个 settings.proto 文件，当然名字可以自己取。
```
syntax = "proto3";

option java_package = "dev.shuanghua.datastore";
option java_multiple_files = true;


message SettingsDataStore {
    int32 theme = 1;
}
```
2. 重新构建项目，生存 SettingsDataStore Java 文件


3. 序列化 -> 定义一个 object 类， 实现 Serializer<T> 接口，其中 T 是 settings.proto 文件中定义的类型，也就是上面 message 后面的 SettingsDataStore
```
class SettingsSerializer @Inject constructor() : Serializer<SettingsDataStore> {
    override val defaultValue: SettingsDataStore = SettingsDataStore.getDefaultInstance()

    override suspend fun writeTo(t: SettingsDataStore, output: OutputStream) = t.writeTo(output) // 写入

    override suspend fun readFrom(input: InputStream): SettingsDataStore {
        try {
            return SettingsDataStore.parseFrom(input) // 读取
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }
}
```


4. 在 Repository 数据仓库中调用它的写入和读取函数
```
val settingsDataFlow: Flow<Int> context.SettingsDataStore.datastore
    map { settings: SettingsDataStore ->
        settings.theme
    }

suspend fun setTheme(tm: Int) {
    context.SettingsDataStore.updateData { currentSettings ->
        currentSettings.toBuilder().setTheme(tm).build()
    }
}
```


































