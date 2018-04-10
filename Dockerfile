FROM java:9

LABEL maintainer="Evgeniy Gryaznov"
LABEL email="e.gryaznov@icloud.com"

COPY out/artifacts/kindred_jar/kindred.jar app/kindred.jar

ENV DISPLAY="/private/tmp/com.apple.launchd.Aa6k6TNQw1/org.macosforge.xquartz:0"

CMD ["usr/bin/java", "-jar", "app/kindred.jar"]
