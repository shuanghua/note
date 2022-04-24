---
title: Shadowsocks优化
tags:
  - Linux
id: 4
categories:
  - Linux
toc: true
comments: true
description: Shadowsocks优化
date: 2016-2-12
---

# 1. 增加 TCP 连接数量

编辑 limits.conf（所有命令不要复制前面的阿拉伯序号）
```
vi /etc/security/limits.conf
```

在最末尾回车另起一行增加以下内容 包括*号：
```
* soft nofile 51200
* hard nofile 51200
```

执行以下命令(每次重启都要执行一次才可以)：
```
ulimit -n 51200
```


# 2.查看系统可用算法

```
sysctl net.ipv4.tcp_available_congestion_control
```
得到：et.ipv4.tcp_available_congestion_control = cubic reno 说明我们系统的 hybla 算法没有启用（DigitalOcean等KVM自带有hybla模块算法）我们需要启用它：
```
/sbin/modprobe tcp_hybla
```


# 3.启用 hybal
针对高延迟网络环境

```
vi /etc/sysctl.conf
```

在最末尾回车另起一行加入以下内容：
```
fs.file-max = 51200
# 提高整个系统的文件限制
net.core.rmem_max = 67108864
net.core.wmem_max = 67108864
net.core.netdev_max_backlog = 250000
net.core.somaxconn = 3240000
net.ipv4.tcp_fastopen = 3
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_tw_recycle = 0
net.ipv4.tcp_fin_timeout = 30
net.ipv4.tcp_keepalive_time = 1200
net.ipv4.ip_local_port_range = 10000 65000
net.ipv4.tcp_max_syn_backlog = 8192
net.ipv4.tcp_max_tw_buckets = 5000
net.ipv4.tcp_fastopen = 3
net.ipv4.tcp_rmem = 4096 87380 67108864
net.ipv4.tcp_wmem = 4096 65536 67108864
net.ipv4.tcp_mtu_probing = 1
net.ipv4.tcp_congestion_control = hybla
```

最后（按键盘 Esc 键退出编辑模式，输入:wq 回车，保存并退出）

执行以下命令让上面的配置生效：
```
sysctl -p
```

检查是否生效：
```
sysctl net.ipv4.tcp_fastopen
```
得到：net.ipv4.tcp_fastopen = 3 表示成功生效。



# 4.黑科技 FinalSpeed 出场
堪称 youtube 神器。（本想用锐速的，但官网已暂停注册了

## FinalSpeed安装与说明：（虽然论坛上有，但我还是整理的写一下吧

附上官方安装教程地址：[http://www.ip4a.com/t/515.html](http://www.ip4a.com/t/515.html)

## 说明：
FinalSpeed 必须服务端和客户端同时配合使用,否则没有任何加速效果.
服务器64M-128M内存即可稳定运行,搬瓦工由于存在超售问题至少要256M.
openvz 架构只支持 udp 协议.
服务端可以和锐速共存,互不影响.


## 开始安装：
因为我们使用的是firewall 防火墙，论坛教程中的注意问题的第一步就可以跳过了，它要加速的端口就是我们 Shadowsocks 端口，我们已经开放过了

```
rm -f install_fs.sh
```
```
wget http://fs.d1sm.net/finalspeed/install_fs.sh
```
```
chmod +x install_fs.sh
```
```
./install_fs.sh 2>&1 | tee install.log
```

然后按 “Ctrl+Z” 退出日志完成安装。


**查看日志命令是：**
```
tail -f /fs/server.log
```


# 5.设置 FinalSpeed 开机启动

```
chmod +x /etc/rc.local
```

```
vi /etc/rc.local
```

加入以下内容：
```
sh /fs/start.sh
```

# 6.设置 FinalSpeed 每晚3点（美国时间）自动重启：

```
crontab -e
```
加入以下内容：
```
0 3 * * *  sh /fs/restart.sh
```


# 7.安装FinalSpeed_install 
回到 Windows 电脑下，安装工具里的 FinalSpeed_install 1.0.exe(过程需要什么环境，点确定即可)
右键管理员打开 FinalSpeed (Win7 下直接打开，Win8、Win10 需要管理员运行)：


# 8.配置 Shadowsocks

然后打开 Shadowsocks ,默认的配置就行

【确保 FinalSpeed 里的“加速端口” = Shadowsocks “主机端”设置服务端口】
【确保 FinalSpeed 里的“本地端口” = 本地 Shadowsocks 客户端里的“服务端口”】

![](http://7xrysc.com1.z0.glb.clouddn.com/SS%E4%BC%98%E5%8C%96%E9%85%8D%E7%BD%AE.png)


# 9.完成 
 浏览器打开 google，FinalSpeed 的右下角状态：连接服务器成功，有时候得等会儿才能连上，对着浏览器狂点吧0.0
>具体的后续问题可以参照论坛里的教程贴子或下方留言或 Google。
