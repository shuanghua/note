package com.sample.slidedrawerlayout.touch

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import timber.log.Timber

fun switchEvent(e: Int): String {
    return when (e) {
        0 -> "Down"
        1 -> "Up"
        2 -> "Move"
        else -> {
            "XXXX"
        }
    }
}

class TouchViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtt: Int = 0
) : FrameLayout(context, attrs, defStyleAtt) {

    // 如果重写开发者了 down 时, 没有"主动"去调用  onInterceptTouchEvent 和 onTouchEvent 或 super.dispatchTouchEvent(ev) ,
    // 分发方法只返回 true 或者 false , 则 onInterceptTouchEvent 和 onTouchEvent 就收不到 该事件 和 后续事件

    // 如果重写了 down , 对于 down 没有 "主动"去调用  onInterceptTouchEvent 和 onTouchEvent , 意味 down 没有被本 view 消费掉
    // 同样因为重写而没有调用 super.dispatchTouchEvent(ev) , 系统默认的 down 也就失去了类似先锋探子的身份,  ViewGroup.java 中的就会把后续的第一个事件(也就是 move)作为先锋探子,

    // 先锋探子是一定要进入 onTouchEvent 查看消费情况的, 如果先锋探子自己被 onTouchEvent 消费, 则后续的事件将经过 dispatchTouchEvent 后, 直接传给 onTouchEvent
    // 对于 super.dispatchTouchEvent(ev) , 当不向下分发先锋探子事件, 则后续的事件默认也不需要分发 ,也就是后续事件不会再经过该分发方法,
    // dispatchTouchEvent 不被调用, onInterceptTouchEvent 和 onTouchEvent 就更不会被调用
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {//是否把事件传递给 子View , false:不传递,则事件传回 父View,
        Timber.d("${switchEvent(ev.actionMasked)} -> Touch.dispatchTouchEvent->")
//        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
//            return true
//        }
        return super.dispatchTouchEvent(ev)
    }

    // 不管拦不拦截 down  , down 都会进入到 onTouchEvent , 如 onTouchEvent 返回 true 消费了 down, 后续的其他事件就不再经过 onInterceptTouchEvent , 而是直接都丢给 onTouchEvent
    // 如 down 的 onTouchEvent 返回 false 不消费, 则后续的事件都不会再走这个 View 的所有事件方法
    // [只要截了序列中的某个事件, 后续的事件就都不会在经过 ]
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Timber.d("${switchEvent(ev.actionMasked)} -> Touch.onInterceptTouchEvent->")
//        if (ev.actionMasked == MotionEvent.ACTION_MOVE) {
//            return true
//        }
        return super.onInterceptTouchEvent(ev)
    }

    // 如果 setOnTouchListener 返回 true 消费 , 则所有事件都不会在这里消费(包括开始的 Down 事件)
    // 但如果 setOnTouchListener 返回 false 不消费 , 则事件先走 setOnTouchListener ,发现 setOnTouchListener 不消费, 然后走这里查看是否消费
    // 如果两个地方都返回 false ,意思都不消费, 则 Down 先经过 setOnTouchListener, 然后在经过这里, 且后续的事件都不再经过本 View
    // [ setOnTouchListener 的 onTouchEvent 的优先级比这里 "高" , 也就是说只要 setOnTouchListener 不为空, setOnTouchListener中的 onTouchEvent 都会被调用 ]
    // onTouch 是 onTouchEvent 的爹, onTouch 能决定  onTouchEvent 能不能被调用  , 而 onTouchEvent 是 onClick 的爹 , onTouchEvent 决定 onClick 能不能被调用
    // onTouch 返回 true , onTouchEvent 不会被调用, 也导致 onClick 不会被调用

    // 一但重写了 onTouchEvent , onTouchEvent 和 onTouch 无论返回 true 还是 false , onClick 都不会执行 ,
    // 因为开发者打算自己消费, 重写覆盖掉了 View.java 源码中的 onClick 调用, 要想 onClick 能被继续调用, 则需要返回 super.onTouchEvent(event) 或者调用 super.onTouchEvent(event) + 返回 true
    // 无论 ViewGroup 是否拦截事件, onTouchEvent 只要返回 true 就都可以消费所有事件

    // Down 事件,在默认不重写任何方法的情况下,Down 像探子一样, 都会经过 dispatchTouchEvent , onInterceptTouchEvent, onTouchEvent ,
    // 探子 Down 观察哪个 View 的 onTouchEvent 中返回的是消费 true , 之后就把后续的事件给该 View ,
    // 如果 down 遇到 onTouchEvent 返回 false ,则后续的事件都不会再经过该 View 的 dispatchTouchEvent , onInterceptTouchEvent, onTouchEvent,
    // 如果整个 View 树的 onTouchEvent 都不返回 true ,则交给 Activity 的 onTouchEvent

    // onTouch 返回值决定 onTouchEvent 能不能被调用 , onTouch 返回 true 则不调用
    // super.onTouchEvent(ev) 决定 onClick 能不能被调用
    // 先锋事件被消费, 则后续的事件都会经过 onTouchEvent , 如果先锋事件不被消费, 则后续的事件都不会经过该 view ( dispatchTouchEvent , onInterceptTouchEvent, onTouchEvent 不会被调用了 )
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        Timber.d("${switchEvent(ev.actionMasked)} -> Touch.onTouchEvent")

//        if (ev.actionMasked == MotionEvent.ACTION_MOVE) {
//            Timber.d("${switchEvent(ev.actionMasked)} -> Touch.onTouchEvent")
//            return true
//        }
//        if (ev.actionMasked == MotionEvent.ACTION_UP) {
//            Timber.d("${switchEvent(ev.actionMasked)} -> Touch.onTouchEvent")
//            return false
//        }
        //super.onTouchEvent(ev)  //后面返回 true, 才可以生效以调用 onClick ,
        return super.onTouchEvent(ev)
    }
}

// 默认分发情况下, 不拦截 down , down 默认进入 onTouchEvent , 也不消费 down , 则后续的 move 和 up 都不会再经过这个 view

// 如果 setOnTouchListener 的 onTouch-> false 时, 意味着, 当 down 事件发送到当前 TextView 的 onTouch, down 遇到你设置的是 false ,
// 也就是开发者的你不打算理会 down, down 就转身和 onClick 在一起了, 之后的 move 和 up 也不会再经过你的 setOnTouchListener 中的 onTouch,
// 因为你告诉系统你作为开发者你不打算消费这个事件, 你只要消费了Down , 系统才会给你发后续的其他事件.

// 默认不消费整个事件流程:
// 父分发 -> 父拦截 -> 父分发 -> 父拦截 -> 被触摸的子View分发 -> 被触摸的子View.onTouchEvent -> 父View.onTouchEvent -> 父View.onTouchEvent -> Activity.OnTouchEvent
// (dispatchTouchEvent , onInterceptTouchEvent) -> (dispatchTouchEvent , onInterceptTouchEvent) -> (dispatchTouchEvent, onTouchEvent) -> onTouchEvent -> onTouchEvent
//      ViewGroup1                                                ViewGroup2                                    View                       ViewGroup1      ViewGroup1

// 如果 View 树中都不消费先锋事件,最终被 Activity 消费, 则后续的事件沿最短路径到达 Activity.onTouchEvent

// ViewGroup1 ViewGroup2 View: 如果 ViewGroup2 消费了 down:
// (dispatchTouchEvent , onInterceptTouchEvent) -> (dispatchTouchEvent , onInterceptTouchEvent) -> (dispatchTouchEvent, onTouchEvent)


// 如果 ViewGroup2 消费了 down , 则后续的事件沿最短下发路径进入 ViewGroup2.onTouchEvent, 也就是:
// 此时后续事件: (dispatchTouchEvent , onInterceptTouchEvent) -> (dispatchTouchEvent , onInterceptTouchEvent , onTouchEvent)


// 只有被触摸到的 view 或 ViewGroup 才能收到事件,  如果 ViewGroup 重叠, 则两个 ViewGroup 都能收到事件; 如果是两个重叠的 View, 则最离根部最远的 view 获得

// 事件在回程的时候检查消费情况, 当发现有能消费的他的 View 或 ViewGroup, 则后续的事件在去路的时候直接就进入该 View 的 onTouchEvent

// 事件在去路的时候进入 onTouchEvent ,如果发现不能消费,则直接调用 Activity.onTouchEvent
