---
title: Android笔记：View 点击事件的传递
id: 2
date: 2015-9-12
tags: Android

toc: true
comments: true
description: Android笔记：View 点击事件的传递
---

## 方法介绍:

- boolean dispatchTouchEvent(MotionEvent ev) {} 

//负责分发事件，如果事件能传递到当前View, 当前 View的此方法一定会被调用

----------

- boolean onInterceptTouchEvent(MotionEvent event){} 

//负责拦截事件，一旦拦截，在同一个事件序列中此方法不会再被调用，返回 true 拦截，false 不拦截。

----------

- boolean onTouchEvent(MotionEvent event){} 

//负责消耗事件，例如处理点击事件,返回 true 代表消耗了此事件。

<!-- more -->

## 传递规则和结论:

- ViewGroup 的 onInterceptTouchEvent() 返回 true :且 mOnTouchLister 被设置,则 onTouch 会被调用;
  mOnTouchLister 不被设置,onTouch 就会返回false,则 onTouchEvetn() 被调用, 且如果还设置了 mOnClickListener 的话, onClic() 被调用。

- ViewGroup 的onInterceptTouchEvent() 返回false:交给分发方法(dispatchTouchEvent)执行

- ViewGroup 默认不拦截任何事件

- View 默认消耗事件,即 onTouchEvent() 返回 true ,除非给 View 设置里不可点击

- 子 View 不消耗事件( onTouchEvent 返回 false ):父 View 会被调用.所有的 View 都不消耗,最终传给 Activity 处理,此时 Activity 的 onTouchEvent 被调用

- onClick 触发的前提是:当前 View 是可点击的,并且 down 和 up 执行了

## View的滑动冲突:

- 一般冲突的发生情况:  [外部左右滑动+内部上下滑动]      [外部上下滑动+内部左右滑动]      [外部上下滑动+内部上下滑动]

- 滑动冲突了,那到底交给谁来拦截并消耗呢?

> 答案:水平和竖直方向的滑动距离、速度、以及角度来判断。

## 解决事件冲突:

> 通常：重写父控件的拦截方法 (onIntercepTouchEvent() ),在 move 的时候根据需要进行拦截(返回 true ),比如左右滑动时返回拦截,上下滑动不拦截(传递交给子 View 进行上下滑动的消耗工作了`(*∩_∩*)′,就是分好工这么个意思…..)
