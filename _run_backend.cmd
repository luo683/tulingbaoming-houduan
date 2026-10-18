@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot
cd /d "E:\图灵 报名功能\后端\tuling"
C:\apache-maven\bin\mvn.cmd spring-boot:run > "E:\图灵 报名功能\后端\tuling\backend.log" 2>&1