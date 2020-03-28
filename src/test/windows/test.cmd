@echo off
echo Running %~n1
set "expected=expected\%~n1"
call windows\run.cmd %1 > output.txt
call fc /C /L output.txt %expected% > NUL 2>&1
if errorlevel 1 goto error
goto end

:error
echo Expected:
cat %expected%
echo.
echo Actual:
cat output.txt

:end
del output.txt

