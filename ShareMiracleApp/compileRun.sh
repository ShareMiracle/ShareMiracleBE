#!/bin/bash

# 定义要查询的端口号
PORT=8080

# 使用 lsof 查找使用指定端口的进程并提取其 PID
PID=$(lsof -i :$PORT -t)

if [ -z "$PID" ]; then
  echo "No process is using port $PORT."
else
  echo "Process using port $PORT has PID: $PID"
  kill $PID
  echo "has killed"
fi

echo "compile mvn..."
mvn clean install
echo "compile finished"

java -jar ./ShareMiracle-server/target/ShareMiracle-server-1.0-SNAPSHOT.jar

