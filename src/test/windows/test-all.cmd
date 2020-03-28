rem @echo off
FOR %%i IN (success\*.*) DO call windows\test.cmd %%i
    
if errorlevel 1 goto error
exit 0

:error
exit 1
