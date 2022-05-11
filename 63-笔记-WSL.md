- 启用WSL-Debian
  启用程序和功能中打开虚拟机平台和适用于Windows的Linux
  商店下载Debian
  商店下载Windows Subsystem for Linux Preview

- 打开Windows终端
  
  ```
  wsl --update
  wsl --shutdown
  sudo apt update
  ```

- 安装Chrom
  
  ```
  sudo wget --no-check-certificate https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
  sudo apt install ./google-chrome-stable_current_amd64.deb
  ```

把AndroidStudio压缩包下载到 WSL 文件目录中
然后打开命令窗口输入

- 解压命令:
  
  ```
  tar -xvzf android-studio-ide-183.5522156-linux.tar.gz
  ```

- 运行命令:
  
  ```
  ./android-studio/bin/studio.sh &
  ```
