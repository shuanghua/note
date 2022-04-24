---
toc: false
comments: true
title: 小飞机-R
description: ssr 的安装教程，原作者已经删除项目停止维护，这是安装其他人 Fork 的备份代码。
tags:
  - Linux
id: 26
categories:
  - Linux
date: 2017-10-25
---

## 安装
- 下载
```
git clone -b manyuser https://github.com/Ssrbackup/shadowsocksr.git
```

- 初始化：进入ssr跟目录（存放着多用户配置文件，里面还有一层目录是单用户配置的）
```
cd ~/shadowsocksr
bash initcfg.sh
```

## 启用 mudbjson
> 通过 mudbjson 方式配置多用户

- 编辑 userapiconfig.py
```
vi userapiconfig.py
```

- 参照下面修改
```
API_INTERFACE = 'mudbjson' #修改接口类型为 mudbjson
SERVER_PUB_ADDR = '127.0.0.1' #你的服务器真实 IP
```

- 冒号wq 进行保存（也可以利用服务器远程多功能软件直接使用文本编辑)

## 添加用户
> 为不必要的麻烦, 最好是用 python mujson_mgr.py 脚本命令添加。

建议复制到记事本上修改。
```
python mujson_mgr.py -a -u 用户名 -p 端口 -k 密码 -m 加密方式 -O 协议方式 -G 协议参数 -o 混淆方式 -s 单线程限速 -S 端口总限速 -t 总流量 -f "禁止用户访问的互联网端口"
例如：
python mujson_mgr.py -a -u zhangsan -p 2345 -k passward -m chacha20 -O auth_aes128_md5 -G 5 -o tls1.2_ticket_auth_compatible -s 100 -S 300 -t 100 -f "25,465,233-266"
```

- 以下是 各个参数的详细说明
```
操作:
  -a ADD               添加 用户
  -d DELETE            删除 用户
  -e EDIT              编辑 用户
  -c CLEAR             清零 上传/下载 已使用流量
  -l LIST              显示用户信息 或 所有用户信息
选项:
  -u USER              用户名
  -p PORT              服务器 端口
  -k PASSWORD          服务器 密码
  -m METHOD            服务器 加密方式，默认: aes-128-ctr
  -O PROTOCOL          服务器 协议插件，默认: auth_aes128_md5
  -o OBFS              服务器 混淆插件，默认: tls1.2_ticket_auth_compatible
  -G PROTOCOL_PARAM    服务器 协议插件参数，可用于限制设备连接数，-G 5 代表限制5个
  -g OBFS_PARAM        服务器 混淆插件参数，可省略
  -t TRANSFER          限制总使用流量，单位: GB，默认:838868GB(即 8PB/8192TB 可理解为无限)
  -f FORBID            设置禁止访问使用的端口-- 例如：禁止25,465,233~266这些端口，那么这样写: -f "25,465,233-266"
  -i MUID              设置子ID显示（仅适用与 -l 操作）
  -s value             当前用户(端口)单线程限速，单位: KB/s(speed_limit_per_con)
  -S value             当前用户(端口)端口总限速，单位: KB/s(speed_limit_per_user)
帮助：
  -h, --help           显示此帮助消息并退出
}
```


## 开放防火墙端口
- 开放其 tcp 和 udp 端口
```
firewall-cmd --permanent --zone=public --add-port=2345/tcp
```
	```
	firewall-cmd --permanent --zone=public --add-port=2345/udp
	```

- 让端口生效
```
firewall-cmd --reload
```

## 运行与停止

- 后台运行（无log，ssh窗口关闭后也继续运行）
```
./run.sh
```

- 后台运行（输出log，ssh窗口关闭后也继续运行）
```
./logrun.sh
```

- 后台运行时查看运行情况
```
./tail.sh
```

- 停止运行
```
./stop.sh
```

> 新添加用户时，需要重新执行 bash initcfg.sh 刷新。


## 更新
- 进入shadowsocksr目录
```
cd shadowsocksr
```

- 执行
```
git pull
```

- 成功后重启ssr服务，然后进入到目录执行启动命令
```
./run.sh
```