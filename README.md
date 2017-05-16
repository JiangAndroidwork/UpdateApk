# UpdateApk
首先利用bsdiff工具生成差分包，将bsdiff.exe、旧的apk和新的apk放在一个文件夹里面。（bsdiff.exe放在了工程中的bsdiffAPK文件夹中）。

运行cmd,cd到“更新测试”的文件夹下，输入 bsdiff.exe old.apk new.apk update.patch命令回车就会生成patch差分包在文件夹中。

