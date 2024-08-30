---
toc: true
comments: true
title: Android 绘制条形图
tags:
  - Android
id: 17
categories:
  - Android
date: 2017-8-29
---


![](http://7xrysc.com1.z0.glb.clouddn.com/Kotlin%E7%9B%B4%E6%96%B9%E5%9B%BE.png)
<!-- more -->

# 继承 View 重写构造函数
- 这里重写的是三个参数的构造函数，一般情况下按照 Androi 官方在 Java 构造函数中的 this 写法，重写一个就够了。
- 如果需要重写多个，请参照 Kotlin 多个构造函数

```Kotlin

class HistogramView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr){

}
```


# 定义 Paint 和 Path
```Kotlin

class HistogramView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    val mPaint1 = Paint(Paint.ANTI_ALIAS_FLAG) // 用于绘制坐标轴
    val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG) // 用于绘制条形图
    val mPath1 = Path()
  }
```


# 设置 Paint Path
- 设置和初始化放到 init{ } 中
- 像 val typedArray = context.obtainStyledAttributes 这种的都应该放到 init 中

  ```Kotlin

  class HistogramView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

      val mPaint1 = Paint(Paint.ANTI_ALIAS_FLAG) // 用于绘制坐标轴
      val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG) // 用于绘制条形图
      val mPath1 = Path()

      init {
          mPaint1.textSize = 25f
          mPaint1.color = Color.WHITE
          mPaint1.style = Paint.Style.STROKE// 画笔设置为线性
          mPaint1.strokeWidth = 3f //设置画笔的线性宽度

          mPaint2.color = Color.GREEN
          mPaint2.style = Paint.Style.FILL // 画笔设置为填充
      }
}
  ```

# onDraw 绘制
```Kotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * 画直方图
 */
class HistogramView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    val mPaint1 = Paint(Paint.ANTI_ALIAS_FLAG) // 用于绘制坐标轴
    val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG) // 用于绘制条形图
    val mPath1 = Path()

    init {
        mPaint1.textSize = 25f
        mPaint1.color = Color.WHITE
        mPaint1.style = Paint.Style.STROKE// 画笔设置为线性
        mPaint1.strokeWidth = 3f //设置画笔的线性宽度

        mPaint2.color = Color.GREEN
        mPaint2.style = Paint.Style.FILL // 画笔设置为填充
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mPath1.moveTo(100f, 10f) // 绘制路径的起点
        mPath1.lineTo(100f, 600f)// 设置绘制 Y 轴路径
        mPath1.rLineTo(900f, 0f) // 设置绘制 X 轴路径 （相对上一个结束点进行绘制）

        canvas.drawPath(mPath1, mPaint1)// 开始按照上面设置好的路径进行绘制

        canvas.translate(100f, 600f)//设置(100,600)为原点坐标，原来原点坐标为(0,0)

        // 间隔宽度 25 ，条形宽度 100

        canvas.drawRect(25f, -10f, 125f, 0f, mPaint2)
        canvas.drawText("A", 25f, 28f, mPaint1)

        canvas.drawRect(150f, -20f, 225f, 0f, mPaint2)
        canvas.drawText("A", 150f, 28f, mPaint1)

        canvas.drawRect(250f, -21f, 350f, 0f, mPaint2)
        canvas.drawText("B", 250f, 28f, mPaint1)

        canvas.drawRect(375f, -270f, 475f, 0f, mPaint2)
        canvas.drawText("C", 375f, 28f, mPaint1)

        canvas.drawRect(500f, -455f, 600f, 0f, mPaint2)
        canvas.drawText("D", 500f, 28f, mPaint1)

        canvas.drawRect(625f, -500f, 725f, 0f, mPaint2)
        canvas.drawText("E", 625f, 28f, mPaint1)

        canvas.drawRect(750f, -200f, 850f, 0f, mPaint2)
        canvas.drawText("F", 750f, 28f, mPaint1)
    }
}
```
