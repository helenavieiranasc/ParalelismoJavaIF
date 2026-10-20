@echo off
chcp 65001 > nul

where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Users\discente\jdk-17\bin\javac.exe" set "PATH=C:\Users\discente\jdk-17\bin;%PATH%"
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo ============================================================
echo   PROJETO JCRPGPD - TESTE RAPIDO (1 MILHAO DE TRANSACOES)
echo ============================================================
echo Compilando os codigos Java...
javac -encoding UTF-8 -cp "lib/*;." -d bin src/main/java/jcrpgpd/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Houve um problema na compilacao.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilado com sucesso!
echo.
echo Executando com 1.000.000 de transacoes...
echo.
java -cp "bin;lib/*" -Dorg.slf4j.simpleLogger.defaultLogLevel=error --enable-native-access=ALL-UNNAMED jcrpgpd.Main 1000000

echo.
pause
