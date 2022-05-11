---
toc: false
comments: true
title: Retrofit2-笔记
description: retrofit2 使用笔记
tags:
  - Android
id: 35
categories:
  - Android
date: 2018-5-27
---

# URL

## Intereptor
```java
public class CustomInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
		//代码拼接 URL 参数
        HttpUrl httpUrl = request.url().newBuilder()
            .addQueryParameter("token", "tokenValue")
			.addQueryParameter("page","12")
            .build();
        request = request.newBuilder().url(httpUrl).build();
        return chain.proceed(request);
    }
}
```

<!-- more -->

## Get 请求
注解参数拼接 URL 参数

- @Get + Query :参数较少情况

```
val url = https://baidu.com/api/android/index?page=1

@GET("api/android/index")
fun getNetworkData(@Query("page") p: String): Call<ResponseBody>
```

> @Query("page") 的意思就是：查找 url 中的 pape 字段，然后把 p 的值赋值个它。

- @Get + QueryMap :参数较多
```
@GET("api/android/index")
fun getNetworkData(@QueryMap params: Map<String, String>):Call<ResponseBody>
```

- @Get + Path
```
@GET("api/android/{type}/index")
fun getNetworkData(@Path("type") t: String): Call<ResponseBody>
```


## Post 请求

- @POST + @Field
```
@FormUrlEncoded
@POST("api/android/index")
fun getNetworkData(@Field("page") params: String):Call<ResponseBody>
```

或者：
- 
```
@FormUrlEncoded
@POST("api/android/index")
fun getNetworkData(@Field(value = "page",encoded = true) params: String):Call<ResponseBody>
```

- @POST + @FieldMap

> 与 GET 的 QueryMap 差不多，直接 new 一个 map，然后 put 键值对。

- @POST + @Body
```
@POST("api/android/index")
fun getNetworkData(@Body("page") entity: 自定义实体类):Call<ResponseBody>
```
> 把参数写到一个实体类中，然后构造传值。


或者使用自带的 RequestBody：
- 
```
@POST("api/android/index")
fun getNetworkData(@Body("page") body: RequestBody):Call<ResponseBody>
```
-  
```kotlin
	val childParam = JsonObject()
	requestData.addProperty("cityid", "00")
	requestData.addProperty("lon", "22.54678")
	requestData.addProperty("lat", "180.84466")
	
	val rootParam = JsonObject()
	root.addProperty("city", "北京")
	root.add("url的参数名", childParam)
	val body = RequestBody.create(MediaType.parse("application/json"), root.toString())
	return body
```

> RequestBody 是一个很强大的类型，它不仅可以像上面那样上传多个参数的json，它还能传递文件，图片

# RequestBody

MediaType.parse("application/octet-stream")//文件
MediaType.parse("application/json")//json
MediaType.parse("text/plain")//文本


举个上传多张图片的栗子：
```kotlin
	val file1= File(Environment.getExternalStorageDirectory()+"/imgs", "1.png")
    val file2 = File(Environment.getExternalStorageDirectory()+"/imgs", "2.png")

	val imgRequestBody1 = RequestBody.create(MediaType.parse("application/octet-stream"),file1)
	val imgRequestBody2 = RequestBody.create(MediaType.parse("application/octet-stream"),file1)

	val imageMap = HashMap<String,RequestBody>()
	images.put("images\"; filename=\""+file1.getName(), photoRequestBody1)
    images.put("images\"; filename=\""+file2.getName(), photoRequestBody2)

	@POST(upload/files)
	fun uploadFiles(@PartMap imageMap: Map<String,RequestBody>):Call<ResponseBody>
```