---
toc: true
comments: true
description: 从作用域角度去理解 RxJava 的 subscribeOn 和 observeOn
title: subscribeOn和observeOn 简单笔记
tags:
  - Android
id: 25
categories:
  - Android
date: 2017-10-22
---
# subscribeOn

> subscribeOn 作用域是它**前面**的代码和**后面**的代码，遵守作用域冲突**无效原则**。

当第二个 subscribeOn 的作用域和第一个 subscribeOn 的作用域出现了交集（冲突），那么第二个 subscribeOn 的上下作用域是无效的，因为代码已经被前一个接管了，相当于没有写第二个 subscribeOn。



<!-- more -->

正常情况：

```
fun test() {
        Flowable.create(...)
                .map()//-----------------------------------------------io
                .subscribeOn(Schedulers.io())
                .map()//-----------------------------------------------io

                .observeOn(AndroidSchedulers.mainThread())
                .map()//-----------------------------------------------main

                .observeOn(Schedulers.io())
                .map()//-----------------------------------------------io

                .subscribe()//-----------------------------------------最终为 io
    }
```

冲突情况：

```
fun test() {
        Flowable.create(...)
                .map()//-----------------------------------------------io
                .subscribeOn(Schedulers.io())//我前面和后面都归我管
                .map()//-----------------------------------------------io

                .subscribeOn(AndroidSchedulers.mainThread())//前面和后面都归我管，但前面的先被别人管了，所以我的管理无效，后面的也归别人管了。
                .map()//-----------------------------------------------io

                .observeOn(AndroidSchedulers.mainThread()) //我只管我后面的
                .map()//-----------------------------------------------main

                .subscribe()//-----------------------------------------最终为 main
    }
```




# observeOn

> observeOn 作用域是它**后面**的代码，遵守作用域冲突无效原则。

从 observeOn 的作用域定义来看，连续多个 observeOn 的写法不会产生冲突，所以适合做多次线程切换操作。

当 observeOn 之后 subscribeOn 时，作用域就出现了冲突，所以 subscribeOn 会无效。

正常情况：
```Kotlin
fun test() {
		Flowable.create(...)
	    .map()//-----------------------------------------------io
	    .subscribeOn(Schedulers.io())//我前面和后面都归我管
		.map()//-----------------------------------------------io
	
		.observeOn(Schedulers.io()) //我只管我后面的
		.map()//-----------------------------------------------io
	
		.observeOn(AndroidSchedulers.mainThread())
		.map()//-----------------------------------------------main
	
		.observeOn(Schedulers.io())
		.map()//-----------------------------------------------io
	
		.observeOn(AndroidSchedulers.mainThread()) //我只管我后面的
		.map()//-----------------------------------------------main
	
		.subscribe()//-----------------------------------------最终为 main
    }
```

冲突情况：
```
fun test() {
        Flowable.create(...)
                .map()//-----------------------------------------------io
                .subscribeOn(Schedulers.io())//我前面和后面都归我管
                .map()//-----------------------------------------------io

                .subscribeOn(AndroidSchedulers.mainThread())//前面和后面都归我管，但前面的先被别人管了，所以我的管理无效，后面的也归别人管了。
                .map()//-----------------------------------------------io

                .observeOn(Schedulers.io()) //我只管我后面的
                .map()//-----------------------------------------------io

				.observeOn(AndroidSchedulers.mainThread())
                .map()//-----------------------------------------------main
				.subscribeOn(Schedulers.io())//前面又被别人管了，所以无效。
                .map()//-----------------------------------------------main

				.observeOn(AndroidSchedulers.mainThread()) //我只管我后面的
                .map()//-----------------------------------------------main

                .subscribe()//-----------------------------------------最终为 main
    }
```

# 总结

> observeOn 管后面。

> subscribeOn 管前面和后面：

- 当 subscribeOn **前面出现冲突**，先到先得，同时 subscribeOn 设置**无效**
```
Flowable.create(...)
        .map()//-----------------------------------------------io
        .subscribeOn(Schedulers.io())//我前面和后面都归我管
        .map()//-----------------------冲突了，先到先得 ---------------io
        .subscribeOn(AndroidSchedulers.mainThread())//我后到的，前面给你，后面也给你。
        .map()//-----------------------------------------------io
        .subscribe()//-----------------------------------------最终为 main
    }
```

- 当 subscribeOn **后面冲突**，先到先得， subscribeOn **前面**设置**有效**
```
Flowable.create(...)
        .map()//-----------------------------------------------io
		.observeOn(AndroidSchedulers.mainThread())
		.subscribeOn(Schedulers.io())//我后到的，后面出现冲突了，后面给你，我要前面的。
		.map() // 冲突了，先到先得。
        .subscribe()//-----------------------------------------最终为 main
    }
```

- 还有:
```
//这种写法无意义，连续切线程，啥事没干。
Flowable.create(...)
        .map()//-----------------------------------------------io
		.subscribeOn(Schedulers.io())
		.subscribeOn(AndroidSchedulers.mainThread())
		.map()//-----------------------------------------------io
        .subscribe()
    }
```






