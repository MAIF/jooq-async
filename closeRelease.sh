#!/bin/bash

if [[ $1 == "[Gradle Release Plugin] - pre tag commit"* ]]
then
  echo "Closing release"
  ./gradlew closeAndReleaseRepository
else
  echo "Snapshot, doing nothing"
fi
