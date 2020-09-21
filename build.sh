#!/usr/bin/env bash

if [ -z "$TRAVIS_TAG" ]
then
    if test "$TRAVIS_BRANCH" = "master"
    then
        echo 'Master, running test and publishing snapshot'
        sbt -J-XX:ReservedCodeCacheSize=128m ++$TRAVIS_SCALA_VERSION  -Dsbt.color=always -Dsbt.supershell=false test
        echo 'Master, trying to unpublish ...'
        sbt -J-XX:ReservedCodeCacheSize=128m ++$TRAVIS_SCALA_VERSION bintrayUnpublish || true
        #echo 'Master, publishing ...'
        #sbt -J-XX:ReservedCodeCacheSize=128m ++$TRAVIS_SCALA_VERSION publish
    else
        echo 'Not a tag or master, just run test'
        sbt -J-XX:ReservedCodeCacheSize=128m ++$TRAVIS_SCALA_VERSION  -Dsbt.color=always -Dsbt.supershell=false ";test"
    fi
else
    echo "Tag ${TRAVIS_TAG}, Publishing lib"
    sbt -J-Xmx2G -J-Xss20M -J-XX:ReservedCodeCacheSize=128m ++$TRAVIS_SCALA_VERSION  -Dsbt.color=always -Dsbt.supershell=false "publish"
fi