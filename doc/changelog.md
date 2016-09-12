# changelog #

## 0.2 ##

Released 2016-09-13

New features:

- static ([#23](../../../issues/23)) and dynamic ([#27](../../../issues/27)) container linking
  ([static](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleLinkTest.java) and
  [dynamic](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleLinkDynamicTest.java) example)
- ([#24](../../../issues/24)) wait for specific sequence of messages
  (instead single message) in output at container start
  ([example](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleWaitForLogMessageSequenceAtStartTest.java))
- ([#2](../../../issues/2)) wait for http url and tcp socket open on container start
  (examples: [tcp socket wait](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleWaitForTcpPortTest.java)
  and [http wait](../src/test/java/pl/domzal/junit/docker/rule/examples/ExampleWaitForHttpPingTest.java)).
  See [notes](tcp_wait_notes.md)
- ([#29](../../../issues/29)) expose specified container port to random host port
  ([example](../src/test/java/pl/domzal/junit/docker/rule/examples/ExamplePortExposeDynamicTest.java))

## 0.1.1 ##

Released 2016-09-08

Fixes:

-  ([#32](../../../issues/32)) NullPointerException if an image don't have a repoTags

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

