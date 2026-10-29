@echo off
chcp 65001 > nul
echo Avvio di DriveHub in corso (profilo a basso consumo RAM: max 192MB)...
set MAVEN_OPTS=-Xms32m -Xmx192m -XX:+UseSerialGC
mvn javafx:run
pause
