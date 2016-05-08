#!/usr/bin/env bash

#
# command line runner for the weather service REST endpoint
#

function cleanup() {
    kill ${SERVER_PID} ${CLIENT_PID}
    rm -f cp.txt target
}

trap cleanup EXIT

mvn package dependency:build-classpath -Dmdep.outputFile=cp.txt
CLASSPATH=$(cat cp.txt):target/classes
echo Executing server
java -jar target/weather-1.2.0.jar &
SERVER_PID=$$

while ! nc localhost 9090 > /dev/null 2>&1 < /dev/null; do
    echo "$(date) - waiting for server at localhost:9090..."
    sleep 1
done

echo Executing client
java -classpath ${CLASSPATH} com.crossover.trial.weather.client.WeatherClient
CLIENT_PID=$$
cleanup
