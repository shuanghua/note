---
title: Realm for Android 学习
tags:
  - Android
id: 10 
categories:
  - Android
toc: true
comments: true
description: Realm 轻量型、简单、快速，支持 Rxjava1 Gson Kotlin Retrofit 序列化,有云端版本，文档教程和代码例子全面，技术支持有会中文的哦。

date: 2016-12-3
---

Realm 轻量型、简单、快速，支持 Rxjava1 Gson Kotlin Retrofit 序列化,有云端版本
，文档教程和代码例子全面，技术支持有会中文的哦。

对于 Realm 的使用 “强烈” 建议先看官方文档，这里只做简单的笔记介绍和需要注意的地方。
[https://realm.io/cn/docs/java/latest/#section-20](https://realm.io/cn/docs/java/latest/#section-20 "Realm 中文文档")

跨平台：
同一个 Realm 文件不仅仅能被 Java 访问，只要你使用同样的模型定义，它也可以被 Realm Objective-C、Realm React Native、Realm Swift 和 Realm Xamarin 访问。



<!-- more -->

技术支持：
工程师会实时地在 [Stack Overflow](http://stackoverflow.com/questions/tagged/realm) 和 [Github](https://github.com/realm) 提供有价值的答案。

(关注技术支持很重要的哦，例如：Realm 在支持 Rxjava2 方面)


# 配置
## 项目级 build.gradle
 
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "io.realm:realm-gradle-plugin:x.x.x"
    }
}
```

## app级 build.gradle
```
apply plugin: 'realm-android'
```

## Application
```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        //Realm.deleteRealm(realmConfiguration);
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
```

- Realm 不支持嵌套类（即内部类）的数据模型 “Error: The RealmClass annotation does not support nested classes”，就是说正常使用GsonFormat插件自动生成的java bean中有内部类的是不行的。需要单独将内部类提为单类的java文件。

- 类名长度的上限是 57 个字符。Realm-java 在存储时会为对象名添加 class_ 前缀
成员变量名长度上限是 63 个字符；

- 不支持嵌套事务（transaction），使用嵌套事务会导致抛出异常；

- String 和 byte [] 大小不能超过 16MB；

- Realm 文件目前暂不支持多进程访问

- io.realm.internal.OutOfMemoryError 异常有可能会导致 Realm 数据损坏，所以要控制好自身应用内存的使用

- RealmObject.equals() 返回 true 时会有相同的哈希值，但这个哈希值是会改变的，所以不建议将其用 HashMap 或 HashSet 等依赖于不变哈希值的保存方式保存起来


```java
public class NewslistEntity extends RealmObject {

	//@Ignore 注解代表不存储
    @Ignore
    private String ctime;
    @Ignore
    private String description;

    private String picUrl;

    private String title;

    @Ignore
    private String url;

    public String getCtime() {
        return ctime;
    }
```

# 添加数据 （对数据的操作都必须放在 “事务” 中进行，包括查询，修改，删除等）
## 获取 Realm 实例
在onCreate 生命周期或别的地方获取 Realm 实例
```java
mRealm = Realm.getDefaultInstance()
```

## 添加数据 （可同步和异步）
```java
private void saveToRealm(List<NewslistEntity> allData) {
        mRealm.executeTransactionAsync(realm1 -> {
            for (NewslistEntity data : allData) {
                NewslistEntity entity = realm1.createObject(NewslistEntity.class);
                entity.setTitle(data.getTitle());
                entity.setPicUrl(data.getPicUrl());
            }
        }, () -> {
            //Log.d("saveToRealm","Realm--->>写入成功");
        }, error -> {
            //Log.d("saveToRealm","Realm--->>写入出错");
        });
    }
```

后面的成功和出错的方法可以不实现的

```java
private void saveToRealm(List<NewslistEntity> allData) {
        mRealm.executeTransactionAsync(realm1 -> {
            realm1.deleteAll();
            for (NewslistEntity data : allData) {
                NewslistEntity entity = realm1.createObject(NewslistEntity.class);
                entity.setTitle(data.getTitle());
                entity.setPicUrl(data.getPicUrl());
            }
        });
    }
```

这样就存储完成了，是不是比写SQLite语句爽多了。

# 查询（可同步和异步）
## 查询所有数据
```java
private void getFromRealm() {
        RealmResults<NewslistEntity> results = mRealm.where(NewslistEntity.class)
                .findAll();
        mAdapter.addAll(results);
    }
```
RealmResults 是 Realm 模型对象的容器，其行为与 Java 的普通 List 近乎一样。同一个 Realm 模型（Bean）对象可以存在于多个 RealmList。

## 条件查询 
```java
private void getFromRealm() {
	RealmResults<NewslistEntity> results = realm.where(NewslistEntity.class)
                                  .equalTo("picUrl", "你的图片1地址")
                                  .or()
                                  .equalTo("picUrl", "你的图片2地址")
                                  .findAll();
	}
```
## 查询不支持分页
Realm 并不能分批查询，用做分页，但 Realm 查询的速度很快，如果要分页，只能先把需要的数据全部查询出来，然后对该数据集进行分割处理然后显示。

## RealmResults 自动更新
查询得到的数据默认保存在 RealmResults 中，Realm 底层数据的改变会“自动更新” RealmResults。这意味着数据更新时不用我们自己去再次查询获取数据，我们只需要在下载数据时保存就好。

# 修改

首先把需要修改的数据对象查询出来，然后根据该对象对数据进行修改。


# 删除
1首先把数据查询出来，然后遍历，对要删除的对象数据，或者不遍历删掉所有

```java 

final RealmResults<Dog> results = realm.where(Dog.class).findAll();

realm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        results.deleteFirstFromRealm();
        results.deleteLastFromRealm();
        Dog dog = results.get(5);
        dog.deleteFromRealm();

        // Delete all matches
        results.deleteAllFromRealm();
    }
})


```


##Android 多线程问题，
Realm 实例不能跨线程使用，需在新线程重新创建
在新线程中使用 Realm 必须在使用结束后关闭 Realm 实例
在 IntenService 中使用，需要继承 IntentService ，在重写 onHandleIntent 方法中使用 Realm 和关闭 Realm