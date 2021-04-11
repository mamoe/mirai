@echo off
powershell (new-object System.Net.WebClient).DownloadFile( 'https://github.com/iTXTech/mcl-installer/releases/download/2827601/mcl-installer-2827601-windows-amd64.exe','mcl-installer.exe')
del input.txt
REM 安装 Java
echo Y >> input.txt
REM 使用 Java 11
echo 11 >> input.txt
REM 使用 JRE
echo 1 >> input.txt
REM 使用 32 位 JRE 的兼容所有系统, 也方便如果要用 mirai-native
echo x32 >> input.txt
echo Y >> input.txt
echo Y >> input.txt
echo Y >> input.txt
echo Y >> input.txt
echo Y >> input.txt
echo Y >> input.txt
mcl-installer.exe < input.txt
del input.txt
del mcl-installer.exe
cmd /c mcl.cmd --update-package net.mamoe:mirai-api-http --channel stable --type plugin
echo
echo
echo 安装成功, 以后执行 mcl.cmd 即可启动 Mirai Console
echo Installation succeed. Run mcl.cmd to start Mirai Console.
pause