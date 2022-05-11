- Kotlin 接口实现都是不带括号
  
  ```kotlin
  object : CCCC {
  }
  ```

- Kotlin 子类继承父类都是带括号
  
  ```kotlin
  object : TTTT() {
  }
  ```

- Kotlin 子类继承父类, 父类必须 open 修饰, 抽象类不需要

- Kotlin 子类继承重写父类的普通函数, 父类的普通函数必须 open 修饰, 抽象函数和变量不需要 open 修饰
