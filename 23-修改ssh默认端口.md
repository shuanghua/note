---
toc: true
comments: true
description: 
title: Linux 修改默认的 ssh 22 端口
tags:
  - Linux
id: 23-3
categories:
  - Linux
date: 2017-10-10
---




# 修改默认的 ssh 22 端口

- 在 Port 22 下面添加新的端口 (当新的端口可用的时候再回来删掉 22 端口)
```bash
vi /etc/ssh/sshd_config
```
```
Port 22
Port 你的新ssh 端口
```

- 在 firewalld 中开放上面的新端口
```bash
firewall-cmd --permanent --zone=public --add-port=你的端口号/tcp
firewall-cmd --permanent --zone=public --add-port=你的端口号/udp
```

<!-- more -->


- 重启 ssh 服务
```bash
systemctl restart sshd.service
firewall-cmd --reload
```

- 关闭 SELinux 安全模块
> selinux不熟悉的话建议关闭最好,现在很多新的 vps 服务商都默认关闭了 selinux

- 查看 seLinux 是否已经启用, Disable 为关闭
```bash
sestatus
```
具体关闭请自行查看该文章 : [关闭 SELinux ](https://linuxize.com/post/how-to-disable-selinux-on-centos-7/)
上面步骤嫌麻烦的也可以跳过, 下面开始 ss 正题