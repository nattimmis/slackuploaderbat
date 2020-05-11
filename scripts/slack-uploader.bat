@echo off
set filedate=%date:~10,4%-%date:~7,2%-%date:~4,2%
java -jar ./build/libs/slack-uploader-shadow-minified.jar^
 -a SLACK_TOKEN^
 -t "Title %filedate%"^
 -c "#channel"^
 %1
