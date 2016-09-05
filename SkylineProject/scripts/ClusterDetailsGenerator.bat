@echo off
setlocal enableextensions ENABLEDELAYEDEXPANSION
REM Edit this value to change the name of the file that needs splitting. Include the extension.
SET ip=%2
SET clusterFile=%1
SET count=%3
SET port=7000

for /L %%t IN (1,1,%count%) do (
set n=%%t
echo %ip% !port! >> %clusterFile%
set /a port+=1

)
 endlocal
Pause
