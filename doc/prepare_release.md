### release current snapshot ###

Update / checkout

    git fetch
    git checkout master

Build and deploy

    mvn clean deploy -Ptests-all

### release stable ###

Update / checkout

    git fetch
    git checkout 0.1.x

Version numbers

    RELEASE_VER=<release_version>
    NEXT_VER=<next_snapshot_version>

Deploy release

    mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$RELEASE_VER
    git add pom.xml
    git commit -m "[release] prepare release $RELEASE_VER"
    mvn clean verify -Prelease,tests-all
    mvn deploy -Prelease -DskipTests

Prepare for next SNAPSHOT

    mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_VER
    git add pom.xml
    git commit -m "[release] prepare for next development iteration"
    git push origin 0.1.x
