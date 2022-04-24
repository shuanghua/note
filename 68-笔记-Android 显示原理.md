## Activity 相关
Activity -> Window(PhoneWindow) -> DecorView(View -> ViewRootImpl) -> ContentView 
#### 事件分发
dispatchTouchEvent(Activity) -> superDispatchTouchEvent(PhoneWindow) -> superDispatchTouchEvent(DecorView) --> dispatchTouchEvent(ViewGroup 注意:这里的 dispatchTouchEvent 不是 Activity 的那个方法)

Android 的事件分发的源码阅读烦就烦在: 看似两个类中的某个方法名一样, 但是他们根本就不是同一个, 比如 Activity 类中的 dispatchTouchEvent() 和 View 类中的 dispatchTouchEvent(), 他们不是同一个父类或者接口派生而来, 新手很容易产生阅读困扰.

Activtiy 如果不分发, 则交给 Activtiy 的 onTouchEvent 消耗结束本次事件; Activtiy 如果往下分发, 如果下面的 View 也不处理, 最终也传给 Activtiy 的 onTouchEvent 消耗结束本次事件


### WindowManagerService
通过 binder 跨进程把 WindowSession 对象提供给 ViewRootImpl 使用. 
WindowManagerService 和 ViewRootImpl 是双向通信.
分配 surface , 管理 surface 显示的位置, 顺序及大小.
管理窗口动画.
管理输入事件的分发工作.

#### Window 
在调用 setConteView 的时候创建 PhoneWindow, PhoneWindow 负责界面布局结构的创建并解析 xml 布局文件, 最终把 xml 转换成 java 对象, DecorView 持有这个 java view 对象[通过 addVie() 持有],

#### DecorView
statusBar + navBar + contentView , 每个 Activity 对应一个 DecorView, DecorView 中创建了 ViewRootImpl , DecorView 本身其实就是个 View; DecorView 从结构来看可以当做是打通 View , ViewRootImpl , Window 和 Activity 的关键枢纽, 其中 ViewRootImpl 是这个枢纽的具体实现

#### VewRootImpl
在 Activity onResume 中创建的
VewRootImp 实现了 ViewParent (子 View 和父 View 的布局关联工作) , View.AttachInfo.Callbacks(View 相关的接口) , ThreadedRenderer.DrawCallbacks(底层硬件渲染绘制)
ViewRootImpl 的 setView()函数是专门处理 view 的布局和显示, 它使用 wmSession (详见上面的WindowManagerService)来申请 surface, surface 是界面显示的基础, 而申请 surface 需要向 WMS 申请, 所以需要用到 binder ipc 通信; ViewRootImpl 和 WMS 之间是双向的 binder 通信.

> 创建一个 PhoneWindow , PhoneWindow 创建 DecorView 以及创建界面的布局结构和解析 xml 布局成 java view; DecorView 会持有这个 java view (通过 addVie()持有), 然后 DecorView 把自己传给 WindowManager (通过 wm.addView(decorView) 传递); 在 wm.addVeiw() 函数中通过使用 ViewRootImpl 来布局和显示 view(显示过程最终会用到 binder 驱动来调用).


## 界面刷新相关
由 WMS 管理, 分配 surface  ,然后布局绘制 ,最后交给缓冲区等显示
此处包括一些帧生成, 帧缓冲


## 绘制相关



## Surface