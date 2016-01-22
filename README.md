[![Build Status](https://travis-ci.org/tdomzal/junit-docker-rule.svg?branch=master)](https://travis-ci.org/tdomzal/junit-docker-rule)
# junit-docker-rule #

## What is it ? ##

Simple [JUnit Rule](https://github.com/junit-team/junit/wiki/Rules) starting [docker](https://www.docker.com/) container right from your test case.

You can just type...

    ...
	@Rule
    public static DockerRule testee = DockerRule.builder()
            .imageName("nginx")
            .publishAllPorts(false)
            .expose("8080", "80")
            .build();

    @Test
    public void shouldExposeSpecifiedPort() throws InterruptedException, IOException {
        String nginxHome = "http://"+testee.getDockerHost()+":8080/";
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }
	...

... and tadaaa! container is started just before your test case (and destroyed after).

It was created as side product and I'll be more than happy if you'll find it useful ! 

## What docker options it currently supports ? ##

You can:

- specify image name/tag
- pass environment variables
- publish all exposed port (to dynamically allocated host ports)
- publish specified container ports to specified host ports (no port ranges support yet)
- mount host directory as a data volume (with some restrictions works also under boot2docker!)
- specify extra /etc/hosts entries
- access container stderr and stdout (by default they are passed to java System.err and System.out)
- wait to specific message occuring in container output 

Most of them are shown in [examples](src/test/java/pl/domzal/junit/docker/rule/examples/).

Also - just type `DockerRule.builder().` .. and try code assist in your favorite IDE (Alt-Enter in IDEA, Ctrl-Space in Eclipse) to see all possible options. 

## What do I need to use it ? ##

### 1. Docker (of course) ###

Docker installed and configured - which in general means you must have docker variables (DOCKER\_HOST, DOCKER\_MACHINE\_NAME, ...) available in runtime. Preferred way to set them is via [docker-machine](https://docs.docker.com/machine/) command.

### 2. Make library available for your maven projects ###

Library it is not (yet) publicly available in maven central so you must build it yourself. 

Assuming you have apache maven installed it's just:

	git clone https://github.com/tdomzal/junit-docker-rule.git
	cd junit-docker-rule
	mvn install -DskipTests

or, if you want to build with tests - substitute last command with:

	mvn install

Of course **test cases will run only if you have working docker environment**. 

### 3. Declare in you pom.xml ###

    <dependency>
        <groupId>pl.domzal</groupId>
        <artifactId>junit-docker-rule</artifactId>
        <version>0.1-SNAPSHOT</version>
		<scope>test</scope>
    </dependency>

### 4. Use ###

	import pl.domzal.junit.docker.rule.DockerRule;

	public class MyTestCase {
		...
		@Rule
		public static DockerRule testee = DockerRule.builder()
            .imageName("nginx")
            // ... other build options (try code assist from your IDE to explore available options)
            .build();
		...
		// your test cases

	}
	

## What else should I know for now ? ##

- It uses java [docker client from Spotify](https://github.com/spotify/docker-client)
- Build and tested with docker 1.9
- This is work in progress (but all features are verified by tests)
- It's snapshot version so it's quite possible API may slightly change
- Stable version it's on his way to maven central..
