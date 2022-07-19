---
toc: true
comments: true
description: CentOS 7 编译源码安装 ss-libev
title: CentOS 7 编译安装 ss-libev
tags:
  - Linux
id: 23
categories:
  - Linux
date: 2017-10-7
---

CentOS7 编译源码安装 ss-libev, 文章最近更新于 **2020年7月20日**

<!-- more -->

以下主要内容参考来源于 ss-libev github 官方教程: [https://github.com/shadowsocks/shadowsocks-libev](https://github.com/shadowsocks/shadowsocks-libev)

# 更新系统

- centos7
  
  ```bash
  yum update -y
  ```

- debian
  
  ```bash
  sudo apt update -y
  ```

# 安装常用工具(debian 请使用 sudo apt 替代 yum)

```bash
yum install git wget vim -y
```

# 安装 ss-libev 编译工具

- centos
  
  ```bash
  yum install epel-release gcc gettext autoconf libtool automake make asciidoc xmlto c-ares-devel libev-devel pcre-devel -y
  ```

- 这里也放一下 ubuntu / debian 系列的
  
  ```bash
  sudo apt-get install --no-install-recommends gettext build-essential autoconf libtool libpcre3-dev asciidoc xmlto libev-dev libc-ares-dev automake -y
  ```

- 单独安装 libsodium (主要用于加密、解密、签名和生成密码哈希)
  
  > 点击这里查看 [Libsodium 最新版本](https://download.libsodium.org/libsodium/releases/ "Libsodium"), 如果你怕出问题,那么请跟着 ss-libev 官方所给 1.0.16 的版本来使用.

- ```bash
  export LIBSODIUM_VER=1.0.17
  wget https://download.libsodium.org/libsodium/releases/libsodium-$LIBSODIUM_VER.tar.gz
  tar xvf libsodium-$LIBSODIUM_VER.tar.gz
  pushd libsodium-$LIBSODIUM_VER
  ./configure --prefix=/usr && make
  sudo make install
  popd
  sudo ldconfig
  ```

- 单独安装 mbedtls (SSL和TLS)
  
  ```bash
  export MBEDTLS_VER=2.6.0
  wget https://tls.mbed.org/download/mbedtls-$MBEDTLS_VER-gpl.tgz
  tar xvf mbedtls-$MBEDTLS_VER-gpl.tgz
  pushd mbedtls-$MBEDTLS_VER
  make SHARED=1 CFLAGS=-fPIC
  sudo make DESTDIR=/usr install
  popd
  sudo ldconfig
  ```

# 安装 ss-libev (ss 的 c 语言版)

前面一些环境搭建好了，现在正式安装我们的主角。

- 下载github上的官方源码
  
  ```bash
  git clone https://github.com/shadowsocks/shadowsocks-libev.git
  cd shadowsocks-libev
  git submodule update --init --recursive
  ```

- 编译源码并安装
  
  ```bash
  ./autogen.sh && ./configure && make
  sudo make install
  ```
  
  这里编译可能需要几分钟, 结束后 ss-libev 就算是安装完成了, 结束后需要注意末尾编译信息中 ss-server 文件所在的位置,一般位于 /usr/local/bin/ 目录下.

# 配置 ss 密码

- 创建配置文件
  
  ```bash
  cd ..
  mkdir -p /etc/shadowsocks-libev
  vim /etc/shadowsocks-libev/config.json
  ```

- 复制粘贴以下内容
  
  ```json
  {
  "server": "0.0.0.0",
  "nameserver": "8.8.8.8",
  "server_port": 443,
  "password": "你的密码",
  "method": "chacha20-ietf-poly1305",
  "timeout": 600,
  "no_delay": true,
  "fast_open":false,
  "mode": "tcp_and_udp"
  }
  ```

# 配置防火墙端口

上面设置了一个 443 端口（你也可以改成其它的），现在需要在防火墙上开放这个 443 端口（ 如果不开放，那么所有通过这个 443 端口的外部流量都会被防火墙所拦截 ）

> 如果是 debian 推荐使用 ufw 防火墙 [debian 安装 ufw](https://gist.github.com/shuanghua/b09430e6191520638a1cdc83924a5797)

- 启动 Firewalld 防火墙 (有的 vps 默认安装使用 iptables 这个防火墙 ,关于在 iptables 怎么开放端口请自行搜索折腾)
  
  ```bash
  systemctl enable firewalld
  systemctl start firewalld  //如果这一步执行后卡住, 不用管它,Ctrl+C 结束跳过, 千万不要重启 vps, 等后面端口添加完成之后再重启 vps 即可运行 firewalld
  ```

- 有的 vps 可能没有预装 firewalld ，请先执行下面安装 firewalld 后, 再执行上面命令
  
  ```bash
  yum install firewalld -y
  ```

- 添加开放端口(就是上面配置中的端口号 443 )
  
  ```bash
  firewall-cmd --permanent --zone=public --add-port=你的端口号/tcp
  firewall-cmd --permanent --zone=public --add-port=你的端口号/udp
  ```
  
  **注意：如果你的 ssh 登录的端口不是默认的 22 ,一定要再把你的 ssh 端口也添加上去, 不然你将不能通过 shell 工具登录你的 VPS**

- ssh端口添加完成后需要刷新下 ssh 服务
  
  ```
  systemctl restart sshd.service
  ```

- 使端口生效 （**再看一遍上面的注意提示，如果没有把自定义的‘ vps 的登录的端口’ 加上去，在执行完下面命令后，你可能不能通过 Xshell 等工具登录你的 vps, 最后只能从 vps 服务商的管理后台登录，如果已经登录不了，请在服务商后台登录，然后重新修改端口**）
  
  ```bash
  firewall-cmd --reload
  ```

- 查看已开放的端口（可跳过）
  
  ```bash
  firewall-cmd --list-all
  ```

- 检查 firewalld 运行状态
  
  ```
  systemctl status firewalld
  ```

- 如果前面 start 卡住,那么需要重启一下 vps ,重启之后 firewalld 就会后台正常运行
  
  ```bash
  reboot
  ```

# 运行 ss-libev

到此已经完成所有安装，启动 ss 有两种方式，第一种是通过配置文件 ss-server -c ,另 一种是命令 api 的方式 ss-server -s ；前面我们已经创建好了配置文件, 所以这里使用配置文件的的方式启动。

- 通过使用上面我们创建好的配置文件来启动
  
  ```bash
  ss-server -c /etc/shadowsocks-libev/config.json
  ```

如果上面你的端口已经在防火墙开放了，密码也设置好了；不出意外应该可以遨游世界了。

# 配置开机自启

- 创建一个 Service 服务文件（service 是一种默默在后台认真工作的一种脚本文件）
  
  ```bash
  vim /etc/systemd/system/ss.service
  ```

- 往该文件粘贴以下内容：
  
  ```
  [Unit]
  Description=shadowsocks-libev start service
  After=network.target
  [Service]
  ExecStart=/usr/bin/ss-server -c /etc/shadowsocks-libev/config.json
  Restart=on-abort
  [Install]
  WantedBy=multi-user.target
  ```
  
  注意 ss-server 所在的路径 (有的 linux 系统安装 ss-libev 后是在 /usr/local/bin/ 下)

- 运行该自启服务
  
  ```bash
  systemctl daemon-reload
  systemctl enable ss
  systemctl start ss
  systemctl status ss   //查看运行状态
  ```
  
  每次修改 service 文件都必须执行 systemctl daemon-reload 以重新加载； 只修改 ss 的 config.json 文件的话，只需要要执行 systemctl restart ss

# 问题排查

- **出现 active running** 绿色代表可用（注意：终端的主题不一样，颜色可能不一样以 active running 关键字为准）
  
  ```bash
  systemctl status ss
  ```

- 不能运行时，重启 SS 的自启服务文件试试
  
  ```bash
  systemctl restart ss
  ```

- 显示 active running 但客户端连不上， 请检查端口是否开放，然后重启 VPS
  
  ```bash
  sudo shutdown -r now
  ```

- 重启之后再次查看 SS 运行状态
  
  ```
  systemctl status ss
  ```

- 如果 status 显示运行失败, 执行以下命令检查出错原因
  
  ```
  journalctl -u ss
  ```
  
  
* 如果重启之后，显示 active running，但是客户端依然连接不上，这种情况有可能是开放端口的问题，请尝试文章前面的端口设置部分，尝试更换端口号，同时务必记得向防火墙开放该端口号。

* 如果重启之后，显示的红色，可能是配置文件的格式没有配置正确，或者相关插件配置错误，请仔细查看 Error 部分的日志确定问题所在，比如插件所在的目录，插件的可执行行权限是否正确。

* 仔细检查：编译过程是否出错（再编译一遍） 、 配置文件没有配置正确 、 端口没有开放 、插件目录不正确、 插件不是可执行权限、 插件配置 。

# 更新

- 先停止运行
  
  ```bash
  systemctl stop ss
  ```

- 进入到安装目录进行 pull 更新安装
  
  ```bash
  cd shadowsocks-libev
  git pull
  ./configure make
  make install
  ```

- 更新完记得启动
  
  ```bash
  systemctl start ss
  ```

# 另附

## BBR 优化

- [CentOS 安装 BBR](https://www.vultr.com/docs/how-to-deploy-google-bbr-on-centos-7)

## 插件

- [ss-libev 安装 v2ray-plugin 插件](https://gist.github.com/shuanghua/c9c448f9bd12ebbfd720b34f4e1dd5c6)

## 其它

- [Debian 用 snap 快速安装 ss-libev](https://moshuanghua.com/2019/10/11/23-4-debian10-snap-%E6%8A%98%E8%85%BE/)
- [Ubuntu 16 桌面版 ss-libev 客户端](https://moshuanghua.com/2017/10/09/23-2-ubuntu%E6%A1%8C%E9%9D%A2%E7%89%88%E5%AE%89%E8%A3%85ss/)
- [修改默认的 ssh 22 端口](https://moshuanghua.com/2017/10/10/23-3-%E4%BF%AE%E6%94%B9ssh%E9%BB%98%E8%AE%A4%E7%AB%AF%E5%8F%A3/)