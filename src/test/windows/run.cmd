@echo off
call windows/compile.cmd %1 
call nasm -f win64 ko.asm -o ko.obj 
call link ko.obj /entry:_main /subsystem:console /defaultlib:ucrt.lib /defaultlib:legacy_stdio_definitions.lib /LARGEADDRESSAWARE:NO /libpath:"C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\14.24.28314\lib\onecore\x64" /libpath:"C:\Program Files (x86)\Windows Kits\10\Lib\10.0.18362.0\ucrt\x64" > NUL 2>&1
del ko.asm
del ko.obj
call ko.exe
del ko.exe