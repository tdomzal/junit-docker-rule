# changelog #

## 0.2-SNAPSHOT ##

New features:

- ([#23](../../../issues/23)) add container linking
  ([example](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleLinkTest.java))
- ([#24](../../../issues/24)) add possibility to wait for specific sequence of messages
  (instead single message) in output at container start
  ([example](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleWaitForLogMessageSequenceAtStartTest.java))
- ([#2](../../../issues/2)) add posibility to wait for http url and tcp socket open on container start
  (examples: [tcp socket wait](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleWaitForTcpPortTest.java)
  and [http wait](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleWaitForHttpPingTest.java))

## 0.1 ##

First stable version.
Features:

- use it as JUnit @Rule or @ClassRule
- specify image name/tag
- specify container name (equivalent of command line `--name`)
- pass environment variables (`--env` or `-e`)
- publish all exposed port to dynamically allocated host ports (`--publish-all` or `-P`)
- publish specified container ports to specified host ports (`-p` - tcp or udp, no port
  ranges support yet)
- mount host directory as a data volume (`--volume` or `-v` - also works for workstation
  dirs to boot2docker container with restriction that dir must be under user homedir)
- specify extra /etc/hosts entries (`--add-host`)
- access container stderr and stdout (forwarded to java System.err and System.out by
  default)
- wait for message in output at container start

