@echo off
setlocal enableextensions ENABLEDELAYEDEXPANSION
REM Edit this value to change the name of the file that needs splitting. Include the extension.
SET input=%1
SET cluster=%2
SET count=%3
SET ip=%4
SET port=7000
SET SFX=.txt

for /L %%t IN (1,1,%count%) do (
set  n=%%t
rem set /a n=%%t+10
echo !n!
echo Starting process for port :: !port!
start cmd /k  java -jar KSkyFinder.jar %ip% !port! %cluster% %input%!n!%SFX%
set /a port+=1

)
 endlocal
Pause