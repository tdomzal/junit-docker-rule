## Release check-lists ##

### snapshot ###

Update / checkout

    git fetch
    git checkout master

Build and deploy

    mvn clean deploy -Ptests-all

### stable ###

Version numbers

    BRANCH=0.1.x                # master / 0.r.x
    RELEASE_VER=<release_version>
    NEXT_VER=<next_snapshot_version>

Update / checkout

    git fetch
    git checkout $BRANCH

Deploy release

    mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$RELEASE_VER
    git add pom.xml
    #
    # update README.md dependency snippet with current $RELEASE_VER
    #
    git commit -m "[release] prepare release $RELEASE_VER"
    git tag v$RELEASE_VER
    mvn clean verify -Prelease,tests-all
    mvn deploy -Prelease -DskipTests

Prepare for next SNAPSHOT

    mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_VER
    git add pom.xml
    git commit -m "[release] prepare for next development iteration"
    git push origin $BRANCH v$RELEASE_VER
