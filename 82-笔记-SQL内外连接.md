inner join 内连接

查询 student 表学生name所在 classes表中对应的班级name

结果：条件符合时，得到 student 表 name + classes 表的 name 数据

```sql
SELECT student.name, classes.name 
FROM students // 表1
INNER JOIN classes //连接表2
ON student.class_id = classes.id; //条件
```

```kotlin
data class(
    val studentName: String,
    val className: String
)
```

right outer join 右外连接

结果:  student 表 name + classes 表的 name 数据 + class表不符合条件的行的name字段数据

full outer join 全部连接

根据条件查询所有表的数据，没有对应则以 null 填充



图片:    82


