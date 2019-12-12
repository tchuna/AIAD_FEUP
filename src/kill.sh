#!/bin/bash
while true; do
sleep 5
ps -ef | grep "java -cp ./jade.jar:. Main" | grep -v grep | awk '{print $2}' | xargs kill
done