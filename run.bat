@echo off
setlocal
cd /d "%~dp0"

set "JFX_LIB="
for /d %%D in (rsrc\javafx-sdk-21.0.*) do set "JFX_LIB=%%D\lib"

if not defined JFX_LIB (
  echo Erreur : SDK JavaFX introuvable dans rsrc\. Voir README.
  exit /b 1
)
set "MODULES=javafx.controls"
set "OUT=out"

where java >nul 2>nul
if errorlevel 1 (
  echo Erreur : Java n'est pas installe. Installe un JDK 17+.
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

echo ==^> Compilation...
javac --module-path "%JFX_LIB%" --add-modules %MODULES% -d "%OUT%" src\*.java src\*\*.java
if errorlevel 1 exit /b 1

echo ==^> Lancement...
java --module-path "%JFX_LIB%" --add-modules %MODULES% -cp "%OUT%" Main
