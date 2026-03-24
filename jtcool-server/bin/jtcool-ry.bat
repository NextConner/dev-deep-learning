@echo off
echo.
echo [信息] 使用Jar命令启动JtCool-RuoYi服务。
echo.

cd %~dp0
cd ../jtcool-admin/target

set JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

java -jar %JAVA_OPTS% jtcool-admin.jar

cd bin
pause
