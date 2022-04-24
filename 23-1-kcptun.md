---
toc: true
comments: true
description: centos7 kcptun
title: centos7 kcptun
tags:
  - Linux
id: 23-1
categories:
  - Linux
date: 2017-10-8
---


# centos7 kcptun
> 去 github 下载对应的版本软件：[https://github.com/xtaci/kcptun/releases](https://github.com/xtaci/kcptun/releases)

我是 CentOS7 64 位的系统，所以下载的是 kcptun-linux-amd64-20180305.tar.gz，下载完解压，把 server_linux_amd64 这个文件传到 vps （或者直接通过 vps 下载），然后把该文件移动到 /usr/bin/ 目录下，如果没有执行权限，记得给该文件赋予执行权限。


<!-- more -->


- 创建 kcptun 的配置文件
```bash
vim /etc/kcptun/config.json
```

- 内容如下：
```json
{
    "listen": ":随便写一个端口",
    "target": "127.0.0.1:ss的端口",
    "key": "密码",
    "crypt": "aes-192",
    "mode": "fast2",
    "mtu": 1350,
    "sndwnd": 1024,
    "rcvwnd": 1024,
    "datashard": 70,
    "parityshard": 30,
    "dscp": 46,
    "nocomp": false,
    "acknodelay": false,
    "nodelay": 0,
    "interval": 40,
    "resend": 0,
    "nc": 0,
    "sockbuf": 4194304,
    "keepalive": 10
}
```
具体的相关参数解释可以看官方 github，或者自行 google

- 创建 kcptun 的启动 service 文件
```bash
vim /etc/systemd/system/kcptun.service
```


- 内容如下：
```json
[Unit] 
Description = Kcptun Client Service 
After=network.target
[Service] 
Type=simple 
User=nobody 
ExecStart=/usr/bin/server_linux_amd64 -c /etc/kcptun/config.json
Restart=always 
RestartSec=5
[Install] 
WantedBy = multi-user.target 
```


- 运行该 kcptun 服务
```bash
systemctl daemon-reload
systemctl enable kcptun
systemctl start kcptun
```


- 重启 VPS 
```bash
reboot
```


- 再次查看状态
```bash
systemctl status ss
systemctl status kcptun
```

- 如果在服务器端查看 kcptun 状态，显示提示连接被拒绝，多半是 listener 或 target 配置不对
尝试将 target 修改成以下：
```
 "target": "服务器地址:ss的端口",
```


- 然后重启 kcptun 再看
```bash
systemctl restart kcptun
```