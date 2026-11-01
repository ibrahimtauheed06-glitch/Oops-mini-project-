@echo off
setlocal
echo ===============================================================
echo     COMPILING FOOD WASTE APPLET MODULE (JDK 8)
echo ===============================================================

set "JDK8_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot"
if not exist "%JDK8_HOME%\bin\javac.exe" (
    echo [ERROR] JDK 8 not found at: %JDK8_HOME%
    echo Please verify your JDK 8 installation path.
    pause
    exit /b 1
)

cd /d "%~dp0"

echo Using javac from: %JDK8_HOME%\bin\javac.exe
"%JDK8_HOME%\bin\javac.exe" -encoding UTF-8 -cp "lib\mysql-connector-j-8.0.33.jar" -d . src\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Applet module compiled successfully!
) else (
    echo.
    echo [ERROR] Compilation failed. See error messages above.
)

endlocal
