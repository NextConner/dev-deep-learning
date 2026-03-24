#!/bin/bash

echo ""
echo "[信息] 使用Jar命令启动JtCool-RuoYi服务。"
echo ""

cd "$(dirname "$0")"
cd ../jtcool-admin/target

JAVA_OPTS="-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

java -jar $JAVA_OPTS jtcool-admin.jar
