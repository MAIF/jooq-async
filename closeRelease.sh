if [[ ${{ github.event.head_commit.message }} = "[Gradle Release Plugin] - pre tag commit"* ]]
then
  echo "Closing release"
  ./gradlew closeAndReleaseRepository
else
  echo "Snapshot, doing nothing"
fi
