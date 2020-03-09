@echo off
call ..\..\..\.\gradlew --quiet -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false run -p ../../../ --args="-q" < %1 > ko.nasm
call nasm -f win64 ko.nasm -o ko.obj 
call link ko.obj /entry:main /libpath:"C:\Program Files (x86)\Windows Kits\10\Lib\10.0.17763.0\um\x64" kernel32.lib > NUL 2>&1