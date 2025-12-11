@echo off
cd /d %~dp0..

echo ========================================
echo  Memory Usage Monitor
echo ========================================
echo.

echo Checking Java processes memory usage...
echo.

wmic process where "name='java.exe'" get ProcessId,CommandLine,WorkingSetSize /format:list | findstr /i "ProcessId CommandLine WorkingSetSize" > temp_mem.txt

if exist temp_mem.txt (
    echo Found Java processes:
    echo.
    type temp_mem.txt
    del temp_mem.txt
    echo.
) else (
    echo No Java processes found.
)

echo.
echo ========================================
echo  Total System Memory
echo ========================================
wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /format:list | findstr /i "Free Total"
echo.

echo ========================================
echo  Gradle Daemons
echo ========================================
call gradlew.bat --status
echo.

echo ========================================
echo  Docker Containers
echo ========================================
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>nul
echo.

echo Press any key to exit...
pause >nul
