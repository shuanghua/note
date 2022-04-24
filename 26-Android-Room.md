---
toc: false
comments: true
title: Android Room 使用详解
description: Room 作为 Android Architecture Components 中的一个重要模块,负责数据存储工作，Room 底层依然是 Sqlite 数据库，Room 只是做了抽象封装，通过注解的形式，同时保留原生 Sqlite 语句，让开发者能更好的操作 Sqlite 数据库。
tags:
  - Android
id: 28
categories:
  - Android
date: 2017-11-10
---

# Room

17年，也就是今年，在 Google IO 大会上 ，Google 推出了全新的 Android 组件开发框架：[Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html)

如今 Android Architecture Components 中的  **Lifecycle** 模块已经集成到了 Andorid API 26.1 + 中了.


<!-- more -->

在这里推荐一篇挺好的关于 Android Architecture Components 中文指南：[基于 Android Architecture Components 的应用架构指南](基于Android Architecture Components的应用架构指南 "基于基于Android Architecture Components的应用架构指南的应用架构指南")

我下面不是讲 Android Architecture Components 的使用，而是讲其中的数据存储模块 **Room**.

Room 作为 Android Architecture Components 中的一个重要模块,负责数据存储工作，Room 底层依然是 Sqlite 数据库，Room 只是做了抽象封装，通过注解的形式让开发者能更好的操作 Sqlite 数据库。

简单的入门示例网上有很多，而且都差不多（翻译官网，和 StackOverFlow），没入门的先去 Google 下入门的为好啦。

在使用 Room 便利的一点是：在编译的时候能解决数据库相关使用问题，当然这不包括数据本身。

----------

# 使用
### 建表第一步 @Entity
当你想在数据库中建一张表时，用这个注解修饰你的 Entity 类，Room 就会自动在数据库中生产一张对应的表。

这里我也抄一个网上的常例,然后我们一步一个坑的往里跳：
因为 Room 对应的 Entity 类有一定的要求，首先属性如果是私有的，则必须要有 一个公共且空的的构造函数，同时也需要有相应的 getter 和 setter 方法。如果属性是共有的，则需要有有参的构造函数。这里为了方便，使用 kotlin 的 DataClass 作为例子。

定义一个 Entity 类，data class 自身不能生成空的构造函数，为了让这个 data class 能有空的构造函数，我们对其赋初始值，这样在编译时，它就会产生一个有空构造函数的字节码文件。
```Kotlin
@Entity
data class User(var userId: Int = 0, var name: String = "")
```

- 设置主键：
```Kotlin
@Entity(primaryKeys = arrayOf("userId"))
data class User(var userId: Int = 0, var name: String = "")
```

 或者这样
```Kotlin
@Entity
data class User(@PrimaryKey var userId: Int = 0, var name: String = "")
```

- 主键自增长：
```Kotlin
@Entity
data class User(@PrimaryKey(autoGenerate = true) var userId: Int = 0, 
				var name: String = "")
```

- 设置表名和列名，默认以类名作为表名，默认以对象名作为列名
```Kotlin
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                @ColumnInfo(name = "user") var userId: Int, 
				var name: String)
```


这样我们最最简单的 Entity 就写好了，下面进入建表第二部分


### 建表第二步 @Dao
@**Dao**: Data Access Object,数据访问对象。

建立一个接口，用 @Dao 注解修饰，写上抽象的增删改查方法。
```Kotlin
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun deleteAll()

    @Update
    fun updateUser()

    @Query("SELECT * FROM user")
    fun findAll(): List<User>

    @Query("SELECT * FROM user WHEN user_id = :userId")//通过冒号就能拿到传入的参数
    fun findUser(userId: Int): User
```

具体的实现由 Room 框架帮我们完成。


### 建表第三步 @Database
@**Database** 负责注册数据库表，这个类建议写成单例。

建立一个抽象类继承 RoomDatabase

```Kotlin
@Database(entities = arrayOf(User::class), //被 @Entity 修饰的类都要在这注册
		  version = 1) //数据库版本
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase? = null
        private val APP_DATABASE_NAME = "UserApp.db"

        fun getInstance(context: Context): AppDataBase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDataBase::class.java, APP_DATABASE_NAME)
                        .build()
    }
}
```

### 建表第四步 插入
```Kotlin
val user = User()
user.userId = 1
user.name = "Google"
AppDataBase.getInstance(Activity.this).userDao().insert(user)//必须在子线程中操作
```

OK!,建表伟业正式完成，下面入坑：

----------

# 不支持对象关系存储
Sqlite 虽然支持关系存储，像 GreenDao 等第三方数据库框架在此基础上增加支持 对象关系存储，但 Room 结合对 Android 特点及优化的的考虑，明确的禁止 **对象关系存储**。相信很多小伙伴第一次使用 Room 的时候都会在这里短暂的卡住。

解决这个问题有以下三种方式，每种方式针对不同的环境使用的环境：

### 环境1 - 类型转换器 @TypeConverter
这种方式使用的局限性太大。常使用在 Date 于 Long 数据类型的转换,Room 存储不了 Date 类型，只能转换为 Long 类型进行存储。

这个我就照搬**官方的 java 代码了**：
```Java 
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
```

然后在 AppDatabase 中引用这个转换：
```Java
@Database(entities = {User.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
```

Entity:
```Java
@Entity
public class User {
    ...
    private Date birthday;
}
```


最后看 Dao 里面的添加代码：
```Java
@Dao
public interface UserDao {
    ...
    @Query("SELECT * FROM user WHERE birthday BETWEEN :from AND :to")
    List<User> findUsersBornBetweenDates(Date from, Date to);
}
```

这样 Room 在编译时会自动做转换处理。假如我们 User 里面不是 Date ,而是 List 或 对象，这种转换基本上就歇菜了，或者说要使用这种方式来写代码，会显得非常的笨重。使用转换器的方法最好是两者是可以直接转换的。


### 环境2 -内嵌类 @Embedded

@**Embedded** ：用于类嵌套，将另一个或多个类的数据嵌入到一张表里存储。

##### 一对一：

```Kotlin
/**
 * 一个人养了一只狗.
 */
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                var userId: Int,
                var name: String,
                var dog: Dog? = null)

@Entity
data class Dog(@PrimaryKey var dogId: Int = 0, var name: String = "")
```

当你像上面这么写，在编译时，Gradle Console 毫不留情的给你报了红线。。


而 Room 本身支持内嵌的形式去存储和查询，所以只需加个 @Embedded 注解即可，我们修改代码如下：
```Kotlin
/**
 * 一个人养了一只狗.
 */
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                var userId: Int,
                var name: String,
                @Embedded var dog: Dog? = null)

data class Dog(var dogId: Int = 0, var name: String = "")
```

这样 Dog 里的参数就全部放到 User 中去，实现了一表存储两类。在做查询的时候最需查询 user 表就能拿到全部的数据。

@Embedded 支持自定义列名，如：User 类中有一个 name 字段，Dog 中也有一个 name 字段，在建表时就会出现 列名冲突。所以下面的代码就来了：

```Kotlin
/**
 * 一个人养了一只狗.
 */
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                var userId: Int,
                var name: String,
                @Embedded(prefix = "dog_") var dog: Dog? = null)

data class Dog(var dogId: Int = 0, var name: String = "")
```

@Embedded( prefix = "dog_" ) 

表示在 Dog 属性字段前都加上 "dog_" , 这样存储在数据库里的字段就是 dog_name, User 类中的 name 不变。


### 环境3 - 内嵌集合Entity
@**Relation** ： 用于查询一对多关系，将两个不同表的数据关联成一个新的 POJO，然后让 Room 返回这个 POJO 的数据类型，只能用于 List 或 Set。

##### 一对多：
像下面这种一个人养多只狗，一对多关系的情况，@Embedded 就不支持了：

```Kotlin
/**
 * 一个人养了很多只狗
 */
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                var userId: Int,
                var name: String,
                var dogs: List<Dog>? = null)

data class Dog(var dogId: Int = 0, var name: String = "")
```
像上面的代码使用 Embedded 编译时就会报错。


这种情况就得使用 Relation 连接注解来连接两张表（注意一定时表与表之间才可以使用）,现在重新写我们的代码：

```Kotlin
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                var userId: Int = 0,
                var name: String = "")

@Entity(tableName = dog)
data class Dog(@PrimaryKey(autoGenerate = true)
				var dogId: Int = 0, 
				var dog_userId = 0,
				var name: String = "")
```


上面定义了两张表: user 表和 dog 表 ,在 dog 表添加了一列 user 的 id : dog_userId 作为标记 。处理 user 表中有很多个用户的情况,确保 每只 Dog 都有对应的 User 。当在存储数据的时候，记得一定要为这个 dog_userId 添加值后再保存数据库，否则会造成查询失败。


下面将 user 表和 dog 表进行连接：
```
//将 user 表和 dog 表关联成 UserWithDogs，这个是一个 POJO 类,没有任何注解。
data class UserWithDogs(@Embedded var user: User? = null,
                           @Relation(entity = User::class,
                                     entityColumn = "dog_userId",
                                     parentColumn = "userId")
                           var dogs: List<Dog>? = null)
```

entity：实体，要查询的实体对象，如：entity = User::class，说明 dog 表的信息放置到了 user 表，我们只需要查询 user 表即可拿到所有信息。

entityColumn：和 parentColumn 配合使用。这里表示 dog 表中的哪一列。

parentColumn: 这里表示 user 表中的哪一列。

然后在 UserDao 中写：

```Kotln
@Query("SELECT * FROM user")
fun findAll(): List<UserWithDogs>
```
这样就能查询到 用户与狗，顿时想起联通:老用户与狗.
**插入数据的话得分别进行数据插入.**

还有一种情况：当我想要 user 表的所有信息和 dog 表中的 name 字段信息。
```
data class UserWithDogs(@Embedded var user: User? = null,
                           @Relation(entity = User::class,
                                     entityColumn = "dog_userId",
                                     parentColumn = "userId",
									 projection = arrayOf("name"))//只要 dog 表中的 name 和 user 表的信息
                           var dogs: List<String>? = null)
```
这样就避免了查询无用的数据。

> 上面仅仅时两张表的连接，你也可以连接多张表，比如这个 user 还养了很多只猫，**@Relation** 和 **@Embedded**,结合使用，基本上能解决对象存储问题，但很不推荐这么做，我们尽量的让 POJO 类保持简单简洁，需要上面数据就拿什么数据。

----------


# 外键
- 为了数据的完整性,我们在 dog 表中做外键约束处理:

将 userId 和 dog 表中的 dog_userId 做外键约束关联,也就是 user 表的主键作为 dog 表的外键.

代码:

```Kotlin
@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true)
                var userId: Int,
                var name: String,
                @Ignore var dogs: List<Dog>? = null)

@Entity(tableName = "dog", foreignKeys = arrayOf(ForeignKey(
        entity = User::class,
        parentColumns = arrayOf("userId"),
        childColumns = arrayOf("dog_userId"),
        onDelete = ForeignKey.CASCADE,
        deferred = true)))
data class Dog(@PrimaryKey(autoGenerate = true)
               var dogId: Int = 0,
               var name: String = "",
               @ColumnInfo(name = "dog_userId")
               var userId: Int = 0)
```

在插入数据的时候记得先 给 Dog 类中的 userId 赋值,再进行插入 dog 表的数据,不然会出现外键约束无效失败

----------

# 数据库版本升级
Room 提供了一个名为：Migration 的抽象类，专门负责版本升级。

比如在 某一张表中先添加了一列：
1：先在 Entity 类中添加你的那一列属性
2：修改 AppDatabase 的版本号
3：创建一个 Migration 子类的实例，重写 migrate（） 函数，**在这个函数中，通过 Sqlite 语句来添加某一列。**
```Kotlin
@Database(entities = arrayOf(User::class), //被 @Entity 修饰的类都要在这注册
		  version = 2)//数据库版本
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase? = null
        private val APP_DATABASE_NAME = "UserApp.db"

//-------------------------------------- 版本1~2 ------------------------------------------
		private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
			override fun migrate(database: SupportSQLiteDatabase) {
				//在 user 表中添加一列名为 address 的字段
				database.execSQL("ALTER TABLE user ADD COLUMN address TEXT")
            }
        }
//---------------------------------------------------------------------------------------

        fun getInstance(context: Context): AppDataBase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
					AppDataBase::class.java, 
					APP_DATABASE_NAME)
			.addMigrations(MIGRATION_1_2)//将此版本升级情况添加到构建中
			.build()
    }
}
```

----------

> Room 不支持存储对象关系,其鼓励我们去实现一个干净的 Entity 类和去写一个简单的数据模型,Entity 不同于 Bean ,也不同于 POJO, Entity 着重映射数据库表; 而且 Room 是在子线程访问的数据库,也避免了阻塞 UI;在开发中也应尽量的分配好数据;在一个页面中,内存中的数据尽量不要有与此页面不相干的数据,需要什么就拿什么数据,尽量做到具体；如果再配合 LiveData 做实时 UI 数据,这样数据和 UI 之间的响应达到最块。
