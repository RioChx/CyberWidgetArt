#!/bin/sh

# Gradle Wrapper script - standard from Gradle distribution

set -o errexit
set -o nounset
set -o pipefail

if [ "${DEBUG-}" = "true" ]; then
    set -o xtrace
fi

# Resolve links ($0 may be a symlink)
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done

DIR=$(dirname "$PRG")

APP_HOME=$(cd "$DIR/.." > /dev/null || exit 1; pwd)

# Use JAVA_HOME if set, otherwise use java from PATH
if [ -n "${JAVA_HOME-}" ]; then
    JAVA_EXEC="$JAVA_HOME/bin/java"
else
    JAVA_EXEC="java"
fi

exec "$JAVA_EXEC" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
