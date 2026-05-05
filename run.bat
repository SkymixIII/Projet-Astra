@echo off
setlocal
cd /d "%~dp0"

set "JFX_LIB=rsrc\javafx-sdk-21.0.5\lib"
set "MODULES=javafx.controls"
set "OUT=out"

where java >nul 2>nul
if errorlevel 1 (
  echo Erreur : Java n'est pas installe. Installe un JDK 17+.
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

echo ==^> Compilation...
javac --module-path "%JFX_LIB%" --add-modules %MODULES% -d "%OUT%" src\*.java
if errorlevel 1 exit /b 1

echo ==^> Lancement...
java --module-path "%JFX_LIB%" --add-modules %MODULES% -cp "%OUT%" Main
