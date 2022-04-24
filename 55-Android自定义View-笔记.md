---
toc: false
comments: false
title: Android 自定义 View 学习笔记
description: Android 自定义 View 学习笔记
tags:
  - Android
id: 55
categories:
  - Android
date: 2019-1-11
---

- Paint.setStyle(Style style) 设置绘制模式,FILL, STROKE 和  FILL_AND_STROKE,填充，线条和两者的结合
- Paint.setColor(int color) 设置颜色
- Paint.setStrokeWidth(float width) 设置线条宽度
- Paint.setTextSize(float textSize) 设置文字大小
- Paint.setAntiAlias(boolean aa) 设置抗锯齿开关 或者 new Paint(ANTI_ALIAS_FLAG)
- paint.setStrokeCap(cap) 设置线条端点的形状，Round=圆， Square=方形， Butt=平行
- paint.setTextSize(float textSize)

<!-- more -->

# 测量
```java
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.v("ProgressBarView onMeasure w:", MeasureSpec.toString(widthMeasureSpec));
        //Log.v("ProgressBarView onMeasure h:", MeasureSpec.toString(heightMeasureSpec));

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int height, width;

        if (widthMode == MeasureSpec.EXACTLY) {// 父 View 做了最大尺寸限制： 当子 view 设置了具体的尺寸 或 当子 view 设置为 match_parent
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {// 父 View 做了最大尺寸限制： 当子 view 设置为 wrap_content， 一般情况下只需要做一个最低尺寸的限制
            int minWidth = dp2px(getContext(), 360);
            width = Math.min(minWidth, widthSize);
        } else {// 父 view 没有任何限制，例如 RecyclerView 、 ScrollView , 子 view 可以任意大小,
            width = widthSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            int minHeight = dp2px(getContext(), 100);
            height = Math.min(minHeight, widthSize);
        } else {
            height = heightSize;
        }

        setMeasuredDimension(width, height);
    }
```


# Canvas
## 坐标
- translate(x, y) 设置新的坐标系原点

## 形状

- canvas.drawText(text, x, y, paint) 绘制文字
> x,y 绘制的起点坐标,text:绘制的文本内容，其实文字的绘制不简单。

- drawRect(left, top, right, bottom,paint) 矩形
> 一般情况下 left 和 bottom 的 y 坐标一致；top 和 right x 的坐标一致。

- drawPoint(x,y,paint)  单点
- drawPoints(float[] pts, int offset, int count,Paint paint) 多个点
> offset：排除数组 pts 中的前多少个数值。count：取出数组中多少个数值来组合成坐标)

- drawOval(float left, float top, float right, float bottom, Paint paint) 椭圆
> 默认只能绘制横着的或者竖着的椭圆，不能绘制斜的

- drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) 圆角矩形
> Api >= 21

- drawRoundRect(new Rectf, float rx, float ry, paint) 圆角矩形
> Api < 21

- drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) 弧形或扇形
> left, top, right, bottom 绘制了一个椭圆；startAngle：弧形的起始角度，x 坐标作为水平线，顺时针为正角度。sweepAngle：弧形划过的角度。useCenter：是否连接到椭圆圆心，如果不连接到椭圆圆心，就是弧形，如果连接到椭圆圆心，就是扇形。



## 路径 Path
通过使用 path ，我们可以绘制各种规则或不规则图形

主要通过 canvas.drawPath(Path path, Paint paint) 来绘制,path 确定绘制的形状

- addCircle(float x, float y, float radius, Direction dir) 圆
> dir : 当这个圆和别的 View 重叠时，绘制重叠部分还是不重叠部分,一般画笔设置为填充时有用。

- lineTo(float x, float y) 画直线
> 绝对坐标来绘制

- rLineTo(float x, float y) 画直线
> 相对坐标来绘制
```kotlin
paint.setStyle(Style.STROKE);  
path.lineTo(100, 100); // 由当前位置 (0, 0) 向 (100, 100) 画一条直线  
path.rLineTo(100, 0); // 由当前位置 (100, 100) 向正右方 100 像素的位置画一条直线 
```

- quadTo(float x1, float y1, float x2, float y2) 二阶贝塞尔曲线

- cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) 三阶贝塞尔

- moveTo(float x, float y) 将路径移动到某个位置

- arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) 弧形(不连接圆心)
> forceMoveTo :绘制是要「抬一下笔移动过去」，还是「直接拖着笔过去」，区别在于是否留下移动的痕迹,true直接跳过去，false 连一条直线到该弧线的起点,然后开始绘制这个弧形。一般在绘制该图形之前已经绘制了别的图形会用到。

- addArc(RectF oval, float startAngle, float sweepAngle) 弧形
> 和 arcTo 的区别是，addArc 的 forceMoveTo 默认为 true

- close() 封闭当前子图形（两个路径存在连接）
> 例如我们用 path 描述了 两条连接但方向不同的直线，如果调用 close,那么会形成一个三角形。 或者理解为 close() = linTo(起点坐标)。当画笔为填充状态时，path 默认执行 close()


## Bitmap
必须得先有一个 Bitmap 对象，然后把这个bitmap对象的像素放到新的 Bitmap 上，在绘制到新的 Bitmap 时，你可以控制绘制的坐标

- drawBitmap(Bitmap bitmap, float left, float top, Paint paint) 画 Bitmap
> left， top 左上角的坐标

- drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) 包含 dst 的重叠规则绘制
> RectF 比 Rect 的精度更高

- drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) Matrix 矩阵变换


## 重叠
-  Path.setFillType(Path.FillType ft) 设置填充方式

> 填充重叠部分还是不重叠部分，或者全部填充绘制
 【WINDING：全填充】
 【EVEN_ODD：交叉填充】
 【INVERSE_WINDING：反色全填充】
 【INVERSE_EVEN_ODD：反色交叉填充】
  path 默认值是 WINDING。

> 以上的解释并不全面，

- EVEN_ODD 奇偶数
> EVEN_ODD 翻译过来就是： 奇偶数
> 在图形内任意地方向图形外画一条射线，该直线会与图形相交，记录相交的点数，当相交点的个数为偶数时，表明射线的起点所在区域不是填充区。奇数则是填充区。【主要看射线起点所在的位置，偶数取反】

- WINDING 非零环绕数原则
> 根据画笔的绘制方向，然后和上面一样先画一条射线，该射线碰到顺时针，点数+1；遇到逆时针-1，计算最后的点数，当最后的点数不为0时（包括负数），说明射线起点所在的区域就是填充区；当点数为0时填充该射线起点所在区域的外部。【遇0取反】

#### PorterDuff.Mode



# 效果-颜色
简单的颜色设置有 paint.setColor、canvas.drawColor()，复杂颜色：Shader,Paint 设置 Shader 后，setColor 不会生效

## Shader
一般的我们使用的都是 Shader 的子类,【LinearGradient 线性渐变】【 RadialGradient 辐射渐变】 【SweepGradient】 【BitmapShader】【 ComposeShader】.

> CLAMP: 单色减弱渐变
> MIRROR: 镜面，先将单色反射成双色，然后渐变
> REPEAT: 重复，两种颜色的边界比较明显

- LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, Shader.TileMode tile)
> 两个端点的坐标和颜色，tile:渐变的模式 CLAMP, MIRROR 和  REPEAT

- RadialGradient(float centerX, float centerY, float radius, int centerColor, int edgeColor, TileMode tileMode) 辐射渐变
> 类似于圆圈的渐变，由中心想四周渐变

- SweepGradient(float cx, float cy, int color0, int color1) 扫描渐变
> 类似雷达屏幕的那种

- BitmapShader(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) Bitmpa 着色
> 将 bitmap 按照TileMode两个方向进行着色和拉伸，我们可以利用这个绘制圆形状 Bitmap，先把这个 bitmapShader 生成出来，然后set 给 Paint,最后让 canvas.Circle(paint) 即可

- ComposeShader(Shader shaderA, Shader shaderB, PorterDuff.Mode mode) 混合两个 Shader
> 假如这两个 shader 都是 Bitmap,那最后的绘制就可能会碰到重叠的场景，最后一个参数 PorterDuff.Mode 就是绘制重叠的策略,直接看官网的图片示例：https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html


> 1. ComposeShader : 混合两个 Shader
> 2. PorterDuffColorFilter :增加一个单色的 ColorFilter
> 3. Xfermode :设置绘制内容和 View 中已有内容的混合计算方式(只能使用其子类 PorterDuffXfermode)


## Clip 裁切
- clipRect()
```
canvas.clipRect(left, top, right, bottom);  
canvas.drawBitmap(bitmap, x, y, paint); 
```

-  clipPath()
```
canvas.save();  
canvas.clipPath(path1);  
canvas.drawBitmap(bitmap, point1.x, point1.y, paint);  
canvas.restore();
```

##  变换
#### 简单二维
位移 旋转

- translate(float dx, float dy) 平移
```
canvas.save();  
canvas.translate(200, 0);  
canvas.drawBitmap(bitmap, x, y, paint);  
canvas.restore();  
```

- rotate(float 角度, float px, float py) 旋转
```
canvas.save();  
canvas.clipRect(left, top, right, bottom);  
canvas.drawBitmap(bitmap, x, y, paint);  
canvas.restore();  
```

- scale(float sx, float sy, float px, float py) 放缩
```
canvas.save();  
canvas.scale(1.3f, 1.3f, x + bitmapWidth / 2, y + bitmapHeight / 2);  
canvas.drawBitmap(bitmap, x, y, paint);  
canvas.restore();  
```

#### Matrix 二维变换
> 略


#### Camera 三维变换
旋转、平移、移动相机
- Camera.rotateX()
```
canvas.save();
camera.rotateX(30); // 旋转 Camera 的三维空间  
camera.applyToCanvas(canvas); // 把旋转投影到 Canvas
canvas.drawBitmap(bitmap, point1.x, point1.y, paint);  
canvas.restore();  
```

- Camera.translate(float x, float y, float z) 移动

- Camera.setLocation(x, y, z) 设置虚拟相机的位置


## 文字



## 状态保存
```java
class CustomView extends View{
  @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomView.SaveState myState = new CustomView.SaveState(superState);
        myState.text = this.mText;
        myState.progressRatio = this.mProgressRatio;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SaveState saveState = (CustomView.SaveState) state;
        super.onRestoreInstanceState(saveState.getSuperState());
        this.mText = saveState.text;
        this.mProgressRatio = saveState.progressRatio;
    }

    private static class SaveState extends BaseSavedState {
        String text;
        float progressRatio;

        public SaveState(Parcel source) {
            super(source);
            text = source.readString();
            progressRatio = source.readFloat();
        }

        public SaveState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(text);
            out.writeFloat(progressRatio);
        }

        public static final Parcelable.Creator<CustomView.SaveState> CREATOR
                = new Parcelable.Creator<CustomView.SaveState>() {
            public CustomView.SaveState createFromParcel(Parcel in) {
                return new CustomView.SaveState(in);
            }

            public CustomView.SaveState[] newArray(int size) {
                return new CustomView.SaveState[size];
            }
        };
    }
}
```


