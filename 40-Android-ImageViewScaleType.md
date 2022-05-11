---
toc: false
comments: true
title: ImageView.ScaleType 笔记
description: FIT_XY,FIT_CENTER,CENTER_INSIDE,CENTER_CROP,CENTER
tags:
  - Android
id: 40
categories:
  - Android
date: 2018-7-10
---

#### ImageView.ScaleType.FIT_XY（放大缩小均可）

- 填充满 ImgeView 自身,不留任何空隙,图片宽高大于 ImageView 的宽高，也不会撑破 ImageView;此时 ImageView 的宽高决定图片 的宽高。

#### ImageView.ScaleType.FIT_CENTER（放大缩小均可）

- 先等比例缩放(保持图片的纵横比) 然后显示到 ImageView 的中央; 
- 先比较容器的宽高，取最小值
- 如果容器的宽是最小值（宽小于高）：那么内容的高会以容器的宽作为放大缩小的标准，也就是内容的宽度接近于容器的宽度
- 如果容器的高是最小值（宽大于高）：那么内容的高会以容器的高作为放大缩小的标准，也就是内容的高度接近于容器的高度
  
  > 当使用 fit 时，内容永远不会因为容器的大小改变而影响到自身比例（也就是内容可能会被放大或缩小，但永远不会出现被拉伸的效果），对于其它的 FIT,比如 FIT_END 只是缩放后显示的位置不一样。如果想改变内容的比例大小，可以使用 Glide 自带的裁切，或者自行处理。

#### ImageView.ScaleType.CENTER_INSIDE（缩小）

- 均匀缩放图片（保持图片的纵横比）图片的尺寸始终等于或**小于** ImageViwe 的尺寸 ，图片最大的缩放值是图片自身默认尺寸，不能高于正常尺寸，可以低于正常尺寸。ImageView 的尺寸在**低于**于正常尺寸的情况下，决定图片的尺寸。

#### ImageView.ScaleType.CENTER_CROP（放大）

- 均匀缩放图片（保持图片的纵横比）图片的尺寸始终等于或**大于** ImageViwe 的尺寸 ，但不会把 ImageView 的大小撑破，只是超过的那部分尺寸不显示；图片最小的缩放值是图片自身默认尺寸，可以高于正常尺寸，不可低于正常尺寸。ImageView 的尺寸在**高于**正常尺寸的情况下，决定图片的尺寸。
  
  > 对于 CENTER_INSIDE（缩小） 和 CENTER_INSIDE（放大），当你的图片资源分辨率很小的时候，而你有想让它在 ImageView 上显示大一点 ，这时候应该选择 CENTER_CROP，然后通过增大 ImageView 的尺寸来显示更大的图片; 当你的图片资源分辨率很大的时候，你想显示小一点就选择 CENTER_INSIDE。

#### ImageView.ScaleType.CENTER

使图像在视图中居中，但不执行缩放，要想能看全图片，则必须 ImageView 的宽高大于图片的宽高

总结：使用 scaleType 的时候，主要考虑的有两点：内容的大小和内容在容器中的位置 。比如内容大小超过容器大小时；你是想增大容器大小以让内容完整显示，还是通过缩放内容来让内容完整显示。又当缩放内容的时候，内容应该处于容器的哪个位置呢。