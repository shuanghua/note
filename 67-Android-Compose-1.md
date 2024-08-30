```kotlin
@Composable
fun NewsStory() {
    MaterialTheme {
        val typography = MaterialTheme.typography //活板文字样式
        Column( //类似竖直的 LinearLayout
            modifier = Modifier.padding(16.dp) // 设置内边距
        ) {
            Image(
                painter = painterResource(R.drawable.header),
                contentDescription = null,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(4.dp)),//圆形角落形状
                contentScale = ContentScale.Crop //缩放
            )
            Spacer(Modifier.height(16.dp)) //留白间隔

            Text(
                "A day wandering through the sandhills " +
                     "in Shark Fin Cove, and a few of the " +
                     "sights I saw",
                style = typography.h6)
            Text("Davenport, California",
                style = typography.body2)
            Text("December 2018",
                style = typography.body2)
        }
    }
}
  
```