# kotlin-检查函数

看要检查的参数是一个变量(也可以是一个函数)，还是一个条件语句， 从而来选择使用不同的检查函数。


### 变量-check
- check(条件语句){}   
检查 全局变量 或 局部变量 是否为空, 主要抛出-状态异常: StateException
```kotlin
val name: String? = null
check(name != null) { "name 不能为空" }
```

- checkNotNull(值)  
```kotlin
val annotationProject = project.rootProject.findProject(module_name_annotation)
checkNotNull(annotationProject){ "annotation 模块不存在!" }
```


### 函数参数-require
- require(条件语句){} 
检查 函数参数 是否为空,  主要抛出-参数异常: ArgumentException

- requireNotNull(值)



### 直接抛出异常-error
- error(直接抛出错误信息) 
默认抛出:StateException , 一般用于表达式语句中
```kotlin
class  SendEmailUseCase ( 
    private  val userRepository: UserRepository, 
    private  val emailService: EmailService, 
) { 
    fun  sendMessage (message: String ) { 
        val user = userRepository.findById()?: error( "未找到用户" ) 
        emailService.sendEmail(user.email ， 信息）	
    } 

    fun  processMessage (message: Message ) : String {
       return  when (message.type) {
          "info" -> "INFO: ${message.message} " 
          "warning" -> "WARNING: ${message.message} " 
          "error " -> "错误: ${message.message} " 

           else -> error( "未知消息类型${message.type} " )
    }
}
```



