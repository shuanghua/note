---
title: 超级梯子XSocks（付费）
tags:
  - Linux
id: 1
categories:
  - Linux
date: 2016-4-29
---

![](http://7xrysc.com1.z0.glb.clouddn.com/tizi.jpg)
<!-- more -->
前面我已经写过 FinalSpeed + SS 双边加速的大概使用教程（Shadowsocks优化）
今天将介绍比 FinalSpeed + SS 更加爽的一个哥们—- XSocks
<!-- more -->
首先先附上XSocks的官网地址：[http://www.xsocks.me/](http://www.xsocks.me/ "XSocks官网")

# (一)介绍

1. XSocks（以后简称 XS） 是 FinalSpeed（以后简称 FS） 的高级版
2. XS 具备 FS 的所有功能
3. XS 自带 Socks5 支持 socks5 udp 协议,配合 sockscap64 可加速游戏,无需部署其他软件，也就是说 不用搭配 SS 独立使用。
4. XS 支持传输加密,FS 没有加密
5. XS 客户端可设置加速任意远程服务器,XS 只能加速到 FS 所在服务器
6. XS 支持多用户,设置登录密码
7. XS 运行更稳定,减少断流,无响应情况,性能更好
8. XS 长期维护更新
9. XS 收费的（最低 80 RMB/年），可以使用7天
10. XS 不支持 PAC 规则（判断是国内网址就使用内网访问，不然爬梯子）



# (二)安装配置-服务端（Linux VPS）

```
rm -f install_xsocks.sh
```

```
wget http://www.xsocks.me/xsocks/install_xsocks.sh
```

```
chmod +x install_xsocks.sh
```

```
./install_xsocks.sh 2>&1 | tee install.log
```

>Debian,Ubuntu 下如果执行脚本出错,请切换到 dash,切换方法: sudo dpkg-reconfigure dash 选 no
**不设置密码可以直接跳到第三点**
----------

## 设置密码：

```
mkdir -p /xs/cnf/
```

```
echo 密码 >> /xs/cnf/password
```

重启 XS（设置和修改完密码需要重启 XS）：
```
sh /xs/restart.sh
```

----------

## 删除,修改密码:
编辑密码文件（一行代表一个密码,删除或修改相应行.）：
```
vi /xs/cnf/password
```

按 ‘Esc’ + ‘:’ + ‘wq’ 回车保存退出编辑
```
sh /xs/restart.sh
```

----------

查看密码:
```
cat /xs/cnf/password
```

----------

取消密码:
```
rm -rf /xs/cnf/password
```

```
sh /xs/restart.sh
```

----------

# (三)安装配置-客户端（Windows XS 客户端）


## 配置 XSocks：
客户端下载地址：[http://www.xsocks.me/xsocks/xsocks_server.zip](http://www.xsocks.me/xsocks/xsocks_server.zip)
![](http://7xrysc.com1.z0.glb.clouddn.com/win_xs.png)

----------

## 配置 Windows SocksCap64
下载地址：[http://www.xsocks.me/xsocks/sockscap64_xsocks.zip](http://www.xsocks.me/xsocks/sockscap64_xsocks.zip)


SocksCap64 作用，因为 XS 使用的协议是 Socks5 ，正常软件的使用的大多是 http,所以要配置我们的软件，原理是让我们的软件走 socks5 代理。

例如以 Chrome 浏览器为例，如果不使用 SocksCap64 就得安装一个支持 Socks5 协议的插件 “SwitchyOmega”

![](http://7xrysc.com1.z0.glb.clouddn.com/switchyomega.png)

但这样每个浏览器都要设置，太麻烦。

所以就使用了 SocksCap64

**安装 SocksCap64 完成后打开，直接把你需要爬梯子的软件拖到 SocksCap64 中，然后通过 SocksCap64 的启动选项打开即可**

----------

**小建议**

>VPS 搭建时同时安装 SS 和 XS ，正常浏览网页 SS 够用且又免费。

>打外服游戏或看电影可以用 XS。

>XS 收费，按个人经济自行打算。
