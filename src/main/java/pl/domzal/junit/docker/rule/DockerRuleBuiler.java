package pl.domzal.junit.docker.rule;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.spotify.docker.client.messages.PortBinding;

public class DockerRuleBuiler {

    private static final int WAIT_FOR_DEFAULT_SECONDS = 30;

    private String imageName;
    private List<String> binds = new ArrayList<>();
    private List<String> env = new ArrayList<>();
    private ExposePortBindingBuilder exposeBuilder = ExposePortBindingBuilder.builder();
    private boolean publishAllPorts = true;
    private String[] cmd;
    private String[] extraHosts;
    private String waitForMessage;
    private int waitForMessageSeconds = WAIT_FOR_DEFAULT_SECONDS;
    private boolean keepContainer = false;
    private boolean imageAlwaysPull = false;
    private PrintStream stdoutWriter;
    private PrintStream stderrWriter;

    DockerRuleBuiler(){}

    public DockerRule build() {
        return new DockerRule(this);
    }

    private static String[] nullToEmpty(String[] value) {
        return value==null ? new String[0] : value;
    }

    /**
     * Command to execute on container.
     */
    public DockerRuleBuiler cmd(String... cmd) {
        this.cmd = cmd;
        return this;
    }
    String[] cmd() {
        return nullToEmpty(cmd);
    }

    /**
     * Image name to be used (required).
     */
    public DockerRuleBuiler imageName(String imageName) {
        this.imageName = imageName;
        return this;
    }
    String imageName() {
        if (StringUtils.isEmpty(imageName)) {
            throw new IllegalStateException("imageName cannot be empty");
        }
        return imageName;
    }

    /**
     * Add extra host definitions into containers <code>/etc/hosts</code>.
     * @param extraHosts List of host matching format "hostname:address" (like desribed for 'docker run --add-host').
     */
    public DockerRuleBuiler extraHosts(String... extraHosts) {
        this.extraHosts = extraHosts;
        return this;
    }
    String[] extraHosts() {
        return nullToEmpty(extraHosts);
    }

    /**
     * Make rule to wait for specified text in log on container start.
     *
     * @param waitForMessage Message to wait for.
     */
    public DockerRuleBuiler waitForMessage(String waitForMessage) {
        this.waitForMessage = waitForMessage;
        return this;
    }
    /**
     * Make rule to wait for specified text in log on container start.
     *
     * @param waitForMessage Message to wait for.
     * @param waitSeconds Number of seconds to wait. Rule startup will fail on timeout.
     */
    public DockerRuleBuiler waitForMessage(String waitForMessage, int waitSeconds) {
        this.waitForMessage = waitForMessage;
        this.waitForMessageSeconds = waitSeconds;
        return this;
    }
    String waitForMessage() {
        return waitForMessage;
    }
    int waitForMessageSeconds() {
        return waitForMessageSeconds;
    }

    /**
     * Keep stopped container after test.
     */
    public DockerRuleBuiler keepContainer(boolean keepContainer) {
        this.keepContainer = keepContainer;
        return this;
    }
    boolean keepContainer() {
        return keepContainer;
    }

    /**
     * Force image pull even when image is already present.
     */
    public DockerRuleBuiler imageAlwaysPull(boolean alwaysPull) {
        this.imageAlwaysPull = alwaysPull;
        return this;
    }
    boolean imageAlwaysPull() {
        return imageAlwaysPull;
    }

    /**
     * Host directory to be mounted into container.<br/>
     * Please note that in boot2docker environments (OSX or Windows)
     * only locations inside $HOME can work (/Users or /c/Users respectively).<br/>
     * On Windows it is safer to use {@link #mountFrom(File)} instead.
     *
     * @param hostPath Directory to be mounted - must be specified Unix style.
     */
    public DockerRuleMountBuilderTo mountFrom(String hostPath) throws InvalidVolumeFrom {
        return new DockerRuleMountBuilder(this, hostPath);
    }
    /**
     * Host directory to be mounted into container.<br/>
     * Please note that in boot2docker environments (OSX or Windows)
     * only locations inside $HOME can work (/Users or /c/Users respectively).
     *
     * @param hostDir Directory to be mounted.
     */
    public DockerRuleMountBuilderTo mountFrom(File hostDir) throws InvalidVolumeFrom {
        String hostDirUnixPath = DockerRuleMountBuilder.toUnixStylePath(hostDir.getAbsolutePath());
        return new DockerRuleMountBuilder(this, hostDirUnixPath);
    }
    DockerRuleBuiler addBind(String bindString) {
        binds.add(bindString);
        return this;
    }
    List<String> binds() {
        return binds;
    }

    /**
     * Set environment variable in the container.
     */
    public DockerRuleBuiler env(String envName, String envValue) {
        env.add(String.format("%s=%s", envName, envValue));
        return this;
    }
    List<String> env() {
        return Collections.unmodifiableList(env);
    }

    /**
     * Expose container port to specified host port. By default
     * all container port are exposed to randomly assigned free
     * host ports. <b>Using manual expose disables this so user must
     * expose all required ports by hand</b>.
     * TODO automatic exposure is disabled after single manual exposure
     *
     * @param hostPort Host port internal port will be mapped to.
     * @param containerPort Container port to map to host.
     */
    public DockerRuleBuiler expose(String hostPort, String containerPort) {
        publishAllPorts = false;
        exposeBuilder.expose(hostPort, containerPort);
        return this;
    }
    Map<String, List<PortBinding>> exposePortBindings() {
        return Collections.unmodifiableMap(exposeBuilder.build());
    }

    /**
     * Redefine {@link PrintStream} STDOUT goes to.
     */
    public DockerRuleBuiler stdoutWriter(PrintStream stdoutWriter) {
        this.stdoutWriter = stdoutWriter;
        return this;
    }
    PrintStream stdoutWriter() {
        return stdoutWriter;
    }

    /**
     * Redefine {@link PrintStream} STDERR goes to.
     */
    public DockerRuleBuiler stderrWriter(PrintStream stderrWriter) {
        this.stderrWriter = stderrWriter;
        return this;
    }
    PrintStream stderrWriter() {
        return stderrWriter;
    }

    /**
     * Enable / disable publishing all container ports to dynamically
     * allocated host ports. Publishing is enabled by default.
     * Dynamic port container ports was mapped to can be read after start
     * with {@link DockerRule#getExposedContainerPort(String)}.
     *
     * @param publishAllPorts true if you want all container ports to be published by default.
     */
    public DockerRuleBuiler publishAllPorts(boolean publishAllPorts) {
        this.publishAllPorts = publishAllPorts;
        return this;
    }
    boolean publishAllPorts() {
        return publishAllPorts;
    }

}