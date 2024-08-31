# Android 子线程更新 ui 的那些事

```kotlin
class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.btn_test_ui)

        textView.setOnClickListener {
           it.requestLayout()  // 第一次 requestLayout
            Thread(Runnable {
//                sleep(2000) // 会报错:子线程不能更新 ui 
                textView.text = "AAAAA" // 第二次 requestLayout 是在 子线程
            }).start()
        }
    }
}
```

> 前提， 我们都知道 ui 线程检查是在 ViewRootImpl 的 requestLayout() 方法中实现的


## 上面代码中， 调用 requestLayout 后立刻在子线程中更新 ui 就不会线程检查（不报子线程不能更新 ui 异常），为什么?

这段代码其实有两次 View 的 requestLayout 调用， 第一次是我们主动调用  it.requestLayout(), 第二次是 TextView setText（） 方法源码中调用 requestLayout()。

因为第一次 requestLayout 还没执行完， ViewRootImpl 中的 mLayoutRequested 被赋值为 true

第二次 setText 中， 会先调用 View 的 requestLayout ， 因为 mLayoutRequested 为 true, 所以 !mParent.isLayoutRequested() 为 false,  if 条件不成立， 里面的 mParent.requestLayout() 也就不会执行， 其实也就是 viewRootImpl 的 requestLayout() 方法， 所以第二次 requestLayout 不会触发线程检查。

如果我们延迟执行 setText ， mLayoutRequested 变成了 false, 之后子线程中便会触发 requestLayout() 就会执行线程检查，此时报出异常！

第二次是否要 requestLayout 完全由 TextView 内部代码实现， 通过查看 TextView 源码其实可以发现， 比如大小和内容都没变， TextView 是不会调用 requestLayout 的
如果内容相同， 宽度设置为 wrap_content 那必定触发 requestLayout , 高度随便

## 源码

其实在 TextView 的 checkForRelayout() 方法中， 谷歌开发者已经写了很清晰的注释说明 TextView 什么情况会触发 requestLayout() 了。

```java
// TextView.java
@UnsupportedAppUsage
private void setText(CharSequence text, BufferType type,boolean notifyBefore, int oldlen) {
    // 省略其它代码
    if (mLayout != null) {
        checkForRelayout();
    }
    // 省略其它代码
}
```

```java
// TextView.java
private void checkForRelayout() {
    // If we have a fixed width, we can just swap in a new text layout
    // if the text height stays the same or if the view height is fixed.
    if ((mLayoutParams.width != LayoutParams.WRAP_CONTENT
            || (mMaxWidthMode == mMinWidthMode && mMaxWidth == mMinWidth))
            && (mHint == null || mHintLayout != null)
            && (mRight - mLeft - getCompoundPaddingLeft() - getCompoundPaddingRight() > 0)) {
        // Static width, so try making a new text layout.
        int oldht = mLayout.getHeight();
        int want = mLayout.getWidth();
        int hintWant = mHintLayout == null ? 0 : mHintLayout.getWidth();
        /*
        * No need to bring the text into view, since the size is not
        * changing (unless we do the requestLayout(), in which case it
        * will happen at measure).
        */
        makeNewLayout(want, hintWant, UNKNOWN_BORING, UNKNOWN_BORING,
                    mRight - mLeft - getCompoundPaddingLeft() - getCompoundPaddingRight(),
                    false);
        if (mEllipsize != TextUtils.TruncateAt.MARQUEE) {
            // In a fixed-height view, so use our new text layout.
            if (mLayoutParams.height != LayoutParams.WRAP_CONTENT
                    && mLayoutParams.height != LayoutParams.MATCH_PARENT) {
                autoSizeText();
                invalidate(); // 只重绘， 然后 return 结束
                return;
            }
            // 动态高度， 但是高度没有变化
            // Dynamic height, but height has stayed the same,
            // so use our new text layout.
            if (mLayout.getHeight() == oldht
                    && (mHintLayout == null || mHintLayout.getHeight() == oldht)) {
                autoSizeText();
                invalidate();  // 只重绘， 然后 return 结束
                return;
            }
        }
        // 动态高度， 高度变化了，我们只能 requestLayout() 了
        // We lose: the height has changed and we have a dynamic height.
        // Request a new view layout using our new text layout.
        requestLayout();
        invalidate();
    } else {
        // 动态宽度， 我们只能 requestLayout() 了
        // Dynamic width, so we have no choice but to request a new
        // view layout with a new text layout.
        nullLayouts();
        requestLayout();
        invalidate();
    }
}
```

再看看 TextView 的父类 View 的 requestLayout() 方法

```java
// View.java
public void requestLayout() {
    // 标记1
    if (mParent != null && !mParent.isLayoutRequested()) {
        // 这里会涉及遍历的过程， 往上 mParent 最终就是 ViewRootImpl
        mParent.requestLayout(); // mParent : ViewParent 接口 (面向接口编程)
    }
}
```

这个 View 的 requestLayout 是 View.java 独有, 和 ViewParent 接口中的 requestLayout() 不是继承关系， 也不是实现关系

```java
// ViewRootImpl.java
@Override
public void requestLayout() {
    // 要想子线程更新 ui 不报异常,则 mHandlingLayoutInLayoutRequest 必须为 true
    // mHandlingLayoutInLayoutRequest 为 true 则表示已经在处理 layout 请求了,不需要再次处理
    if (!mHandlingLayoutInLayoutRequest) { 
        checkThread();
        mLayoutRequested = true; // 标记2
        scheduleTraversals();
    }
}
```

mParent.requestLayout() 最终进入到 ViewRootImpl 的 requestLayout() 方法

如果要想在子线程中更新 ui 不报错， 只需要让 标记1 处的 if 条件不成立即可， 也就是 mParent.isLayoutRequested() 为 true, 我们第一次在 onClickListener 中调用 it.requestLayout() 了， mLayoutRequested 被赋值为 true （标记2）,  mParent.isLayoutRequested() 也就等于 true

所以我们只要在它还没被重置为 false 的时候， 赶紧在子线程更新 ui， 这样 mParent.isLayoutRequested() 永远是 true， 自然就不会执行 if 里面的 mParent.requestLayout() 代码了。

## 其它

## View 是如何调用 ViewRootImpl 中的方法？

View 并没有实现 ViewParent 接口 , View 通过持有 ViewParent， ViewParent 这个接口是给 ViewRootImpl 实现的（其实也就是面向接口编程）

## ViewRootImpl 是如何调用 View 中方法？

ViewRootImpl 持有 DecorView 实例, DecorView 持有 View 实例 （我们的布局 View 最终是被 set 到 DecorView 中的）， 因此 DecorView 很容易就能调用 View 的方法


