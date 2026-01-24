#!/usr/bin/env sh

# Minimal Gradle wrapper script

DIR="$(cd "$(dirname "$0")" && pwd)"

JAVA_CMD="java"

exec "$JAVA_CMD" -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
