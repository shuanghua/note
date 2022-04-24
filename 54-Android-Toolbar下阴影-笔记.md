---
toc: false
comments: false
title: Toolbar 下边界阴影笔记
description: Toolbar 下边界阴影笔记
tags:
  - Android
id: 54
categories:
  - Android
date: 2019-4-13
---


经常看到 Google 官方的 App 应用程序中使用了“上下滑动列表显示或隐藏 Toolbar 的底部阴影” ，于是很好奇这个是怎么实现的，很多人第一反应当然是监听 RecyclerView 的滚动，当滑动到顶部时，隐藏 View 线条；或者给 Toolbar 设置个背景 .9 图;这些我都尝试过，体验效果还是不如 Google 的实现,经过一段时间之后，偶然间在 Material design 的 Github 的 Issues 里面找到了解决办法、

1. 引入 Material design 库
2. 在 style 中修改 应用主题，改为继承 Material Design 主题
3. 在需要联动的布局文件中的最外层使用 CoordinatorLayout 包裹
4. 在 AppBarLayout 中添加 app:liftOnScroll="true"  和 app:liftOnScrollTargetViewId="@id/recyclerView"
5. RecyclerView 标签添加 app:layout_behavior="@string/appbar_scrolling_view_behavior"

效果请自行下载 Gooble Android 短信应用