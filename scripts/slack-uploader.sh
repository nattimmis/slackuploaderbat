java -jar ./build/libs/slack-uploader-shadow-minified.jar \
    -a SLACK_TOKEN \
    -t "Title `date +%Y-%m-%d`" \
    -c "#channel" \
    "$1"
