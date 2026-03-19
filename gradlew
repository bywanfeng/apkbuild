#!/bin/sh
#
# Gradle wrapper launcher script (Unix)
#

# Resolve APP_HOME
PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then PRG="$link"
  else PRG=$(dirname "$PRG")/"$link"; fi
done
SAVED=$(pwd)
cd "$(dirname "$PRG")" > /dev/null || exit
APP_HOME=$(pwd -P)
cd "$SAVED" > /dev/null || exit

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine Java command
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

# Execute Gradle wrapper — JVM opts passed as separate arguments (NOT via DEFAULT_JVM_OPTS string)
exec "$JAVACMD" \
  -Xmx64m \
  -Xms64m \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
