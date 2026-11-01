@echo off
setlocal
echo ===============================================================
echo     LAUNCHING FOOD WASTE APPLET IN APPLETVIEWER (JDK 8)
echo ===============================================================

set "JDK8_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot"
if not exist "%JDK8_HOME%\bin\appletviewer.exe" (
    echo [ERROR] appletviewer.exe not found at: %JDK8_HOME%\bin\appletviewer.exe
    echo Please verify your JDK 8 installation path.
    pause
    exit /b 1
)

cd /d "%~dp0"

:: Set credentials if not already present in environment
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PASSWORD%"=="" set DB_PASSWORD=root

echo Configuration:
echo   - Codebase Policy : applet.policy
echo   - Applet Document : FoodWasteApplet.html
echo   - Database Target : localhost:3306/food_waste_db
echo.
echo Launching Applet Viewer...
"%JDK8_HOME%\bin\appletviewer.exe" -J-Djava.security.policy=applet.policy FoodWasteApplet.html

endlocal
