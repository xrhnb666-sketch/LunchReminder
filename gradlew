#!/bin/sh

APP_HOME=$(cd "${0%/*}" && pwd -P) || exit
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" "-Dorg.gradle.appname=gradlew" -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
