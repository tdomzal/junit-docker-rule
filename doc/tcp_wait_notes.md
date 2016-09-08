#### TCP port wait notes ####

If you are using wait for TCP port open option:

    @Rule
    public DockerRule httpd = DockerRule.builder()
        .imageName(...)
        .waitForTcpPort(80)
        ...

Check will not work if docker engine forwards port using *docker-proxy* (aka *userland proxy*).
It will report port opening almost instantly and **NOT wait for underlying port opening**.

Additional info:

- [Docker docs / Bind container ports to the host](https://docs.docker.com/engine/userguide/networking/default_network/binding/)
- [Docker docs / daemon options](https://docs.docker.com/engine/reference/commandline/dockerd/)
- [Issue / Make it possible to disable userland proxy](https://github.com/docker/docker/issues/8356)


