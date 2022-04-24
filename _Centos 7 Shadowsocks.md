---
toc: true
comments: true
description: 初次使用 CentOS 7

title: 初次使用 CentOS 7
tags:
  - Linux
id: 3
categories:
  - Linux
date: 2015-11-15
---

## 连接 VPS


### 修改密码

输入命令：
```
passwd
```

输入你的新密码，再确认输入，回车完事，以后就可以使用新密码登录了。

----------

## 更新VPS系统资源
中间输入 y 回车确认。
```
yum update
```

----------

## 安装firewalld 防火墙
中间输入 y 回车确认
```
yum install firewalld firewall-config
```

----------


## 配置防火墙：

启动firewalld防火墙:
```
systemctl start firewalld.service
```

设置firewalld开机自启:
```
systemctl enable firewalld.service
```

查看firewalld运行状态：
```
systemctl status firewalld
```


----------

## 开放端口：
```
firewall-cmd --permanent --zone=public --add-port=22/tcp
```
```
firewall-cmd --permanent --zone=public --add-port=22/udp
```

（2323可以自行设置端口号）
```
firewall-cmd --permanent --zone=public --add-port=2323/tcp
```
```
firewall-cmd --permanent --zone=public --add-port=2323/udp
```

载入设置
```
firewall-cmd --reload
```

查看开放的端口
```
firewall-cmd --list-all
```



# 安装软件

## 安装：
```bash
yum install -y gcc automake autoconf libtool make build-essential curl curl-devel zlib-devel openssl-devel perl perl-devel cpio expat-devel gettext-devel
```

```bash
git clone https://github.com/shadowsocks/shadowsocks-libev.git
```

```bash
cd shadowsocks-libev
```

```bash
./configure && make
```

```bash
make install
```

## 新建配置文件
用于设置密码和端口号

```bash
mkdir -p /etc/shadowsocks
```

```bash
vi /etc/shadowsocks/config.json
```
按键盘的 i  键进入编辑模式,复制以下内容进去

================复制以下内容，不要复制“我”===================
```
{
 "server":"0.0.0.0",
 "server_port":2323,
 "local_address": "127.0.0.1",
 "local_port":1080,
 "password":"yourpassword",
 "timeout":300,
 "method":"aes-256-cfb",
 "fast_open": false,
 "workers": 1
}
```
================复制以上的内容，不要复制“我”==================


## 新建 service 配置文件
```
vi /etc/systemd/system/shadowsocks-server.service
```

复制粘贴加入以下内容：
```
[Unit]
Description=Shadowsocks service
After=network.target

[Service]
Type=simple
User=nobody
ExecStart=/usr/local/bin/ss-server -c /etc/shadowsocks/config.json
ExecReload=/bin/kill -HUP $MAINPID
ExecStop=/bin/kill -s QUIT $MAINPID PrivateTmp=true
KillMode=process
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
```

最后Esc :wq保存退出


## 运行服务并设置为开机自启：

运行
```
systemctl start shadowsocks-server.service
```

开机自启
```
systemctl enable shadowsocks-server.service
```

停止
```
systemctl stop shadowsocks-server.service
```


firewalld状态：  （有绿色的active (running)说明在运行）
```
systemctl status firewalld
```

firewalld启动： 
```
systemctl start firewalld.service
```

firewalld开机启动： 
```
systemctl enable firewalld.service
```

shadowsocks状态： （有绿色的active (running)说明在运行） 
```
systemctl status shadowsocks-server
```

shadowsocks启动：
```
systemctl start shadowsocks-server.service
```

shadowsocks开机自启：
```
systemctl enable shadowsocks-server.service
```
