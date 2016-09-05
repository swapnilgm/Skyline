 @echo off
 setlocal enableextensions ENABLEDELAYEDEXPANSION
REM Edit this value to change the name of the file that needs splitting. Include the extension.
SET BFN=%1.txt
REM Edit this value to change the number of lines per file.
SET LPF=%2
REM Edit this value to change the name of each short file. It will be followed by a number indicating where it is in the list.
SET SFN=%1

REM Do not change beyond this line.

SET SFX=.txt

SET a=0
SET FileNum=1

For /F "delims==" %%l in (%BFN%) Do (
SET /a a+=1
rem echo !a!
echo %%l >> %SFN%!FileNum!%SFX%
if  !a!  EQU %LPF%  (
echo inside
SET a=0
rem echo !a!
SET /a FileNum+=1
)

)
 endlocal
Pause
