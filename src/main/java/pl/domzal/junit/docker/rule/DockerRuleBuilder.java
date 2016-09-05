package pl.domzal.junit.docker.rule;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.rules.RuleChain;

import com.spotify.docker.client.messages.PortBinding;

import pl.domzal.junit.docker.rule.ex.InvalidVolumeFrom;

public class DockerRuleBuilder {

    static final int WAIT_FOR_DEFAULT_SECONDS = 30;

    private String imageName;
    private String name;
    private List<String> binds = new ArrayList<>();
    private List<String> env = new ArrayList<>();
    private List<String> staticLinks = new ArrayList<>();
    private List<Pair<DockerRule,String>> dynamicLinks = new ArrayList<>();
    private Map<String,String> labels = new HashMap<>();
    private ExposePortBindingBuilder exposeBuilder = ExposePortBindingBuilder.builder();
    private boolean publishAllPorts = true;
    private String[] entrypoint;
    private String[] cmd;
    private String[] extraHosts;
    private boolean imageAlwaysPull = false;
    private PrintStream stdoutWriter;
    private PrintStream stderrWriter;

    private String waitForMessage;
    private List<String> waitForMessageSequence = new ArrayList<>();
    private List<Integer> waitForPort = new ArrayList<>();
    private List<Integer> waitForHttp = new ArrayList<>();
    private int waitForSeconds = WAIT_FOR_DEFAULT_SECONDS;

    private StopOption.StopOptionSet stopOptions = new StopOption.StopOptionSet();

    DockerRuleBuilder(){}

    public DockerRule build() {
        return new DockerRule(this);
    }

    private static String[] nullToEmpty(String[] value) {
        return value==null ? new String[0] : value;
    }

    /**
     * (Re)define entrypoint on container.
     */
    public DockerRuleBuilder entrypoint(String... entrypoint) {
        this.entrypoint = entrypoint;
        return this;
    }
    String[] entrypoint() {
        return nullToEmpty(entrypoint);
    }

    /**
     * Command to execute on container.
     */
    public DockerRuleBuilder cmd(String... cmd) {
        this.cmd = cmd;
        return this;
    }
    String[] cmd() {
        return nullToEmpty(cmd);
    }

    /**
     * Image name to be used (required).
     */
    public DockerRuleBuilder imageName(String imageName) {
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
    public DockerRuleBuilder extraHosts(String... extraHosts) {
        this.extraHosts = extraHosts;
        return this;
    }
    String[] extraHosts() {
        return nullToEmpty(extraHosts);
    }

    /**
     * Make rule to wait for specified text in log on container start.
     * Whole log content (from container start) is checked so this condition
     * will work independent of placement in chain of other wait conditions.
     * Rule startup will fail when message will not be found.
     *
     * @param waitForMessage Message to wait for.
     */
    public DockerRuleBuilder waitForMessage(String waitForMessage) {
        assertWaitForMessageNotRedefined();
        this.waitForMessage = waitForMessage;
        return this;
    }
    /**
     * Like {@link #waitForMessage(String)} with specified max wait time.
     *
     * @param waitForMessage Message to wait for.
     * @param waitSeconds Number of seconds to wait.
     */
    public DockerRuleBuilder waitForMessage(String waitForMessage, int waitSeconds) {
        assertWaitForMessageNotRedefined();
        this.waitForMessage = waitForMessage;
        this.waitForSeconds = waitSeconds;
        return this;
    }
    private void assertWaitForMessageNotRedefined() {
        if (this.waitForMessage != null) {
            throw new IllegalStateException(String.format("waitForMessage option may be specified only once (previous wait for message value '%s')", this.waitForMessage));
        }
    }
    String waitForMessage() {
        return waitForMessage;
    }
    int waitForSeconds() {
        return waitForSeconds;
    }

    /** Wait for message sequence starting with given message. */
    public WaitForMessageBuilder waitForMessageSequence(String firstMessage) {
        return new WaitForMessageBuilder(this, firstMessage);
    }
    void waitForMessage(List<String> messageSequence) {
        this.waitForMessageSequence = messageSequence;
    }
    List<String> waitForMessageSequence() {
        return waitForMessageSequence;
    }

    /**
     * Keep stopped container after test.
     * @deprecated Use {@link #stopOptions(StopOption...)} instead.
     */
    public DockerRuleBuilder keepContainer(boolean keepContainer) {
        if (keepContainer) {
            this.stopOptions.setOptions(StopOption.KEEP);
        } else {
            this.stopOptions.setOptions(StopOption.REMOVE);
        }
        return this;
    }

    /**
     * Force image pull even when image is already present.
     */
    public DockerRuleBuilder imageAlwaysPull(boolean alwaysPull) {
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
     * @param hostPath Directory or file to be mounted - must be specified Unix style.
     */
    public DockerRuleMountToBuilder mountFrom(String hostPath) throws InvalidVolumeFrom {
        return new DockerRuleMountBuilder(this, hostPath);
    }
    /**
     * Host directory to be mounted into container.<br/>
     * Please note that in boot2docker environments (OSX or Windows)
     * only locations inside $HOME can work (/Users or /c/Users respectively).
     *
     * @param hostFileOrDir Directory or file to be mounted.
     */
    public DockerRuleMountToBuilder mountFrom(File hostFileOrDir) throws InvalidVolumeFrom {
        if ( ! hostFileOrDir.exists()) {
            throw new InvalidVolumeFrom(String.format("mountFrom: %s does not exist", hostFileOrDir.getAbsolutePath()));
        }
        String hostDirUnixPath = DockerRuleMountBuilder.toUnixStylePath(hostFileOrDir.getAbsolutePath());
        return new DockerRuleMountBuilder(this, hostDirUnixPath);
    }
    DockerRuleBuilder addBind(String bindString) {
        binds.add(bindString);
        return this;
    }
    List<String> binds() {
        return binds;
    }

    /**
     * Set environment variable in the container.
     */
    public DockerRuleBuilder env(String envName, String envValue) {
        env.add(String.format("%s=%s", envName, envValue));
        return this;
    }
    List<String> env() {
        return Collections.unmodifiableList(env);
    }

    /**
     * Expose single container port to specified host port.
     * By default all container port are exposed to randomly assigned free
     * host ports. <b>Using manual expose disables this so user must
     * expose all required ports by hand</b>.
     *
     * @param hostPort External host port, container internal port will be mapped to.
     * @param containerPort Container port to map to host.
     */
    public DockerRuleBuilder expose(String hostPort, String containerPort) {
        publishAllPorts = false;
        exposeBuilder.expose(hostPort, containerPort);
        return this;
    }
    /**
     * Expose single container port to dynamically allocated host port.
     * Use {@link DockerRule#getExposedContainerPort(String)} to retrieve host
     * random port number it was allocated.
     * <p>
     * By default all container port are exposed to randomly assigned free
     * host ports. <b>Using manual expose disables this so user must
     * expose all required ports by hand</b>.
     *
     * @param containerPort Container internal port to expose on random host port.
     */
    public DockerRuleBuilder expose(String containerPort) {
        publishAllPorts = false;
        exposeBuilder.expose(containerPort);
        return this;
    }
    Map<String, List<PortBinding>> hostPortBindings() {
        return Collections.unmodifiableMap(exposeBuilder.hostBindings());
    }
    Set<String> containerExposedPorts() {
        return exposeBuilder.containerExposedPorts();
    }

    /**
     * Redefine {@link PrintStream} STDOUT goes to.
     */
    public DockerRuleBuilder stdoutWriter(PrintStream stdoutWriter) {
        this.stdoutWriter = stdoutWriter;
        return this;
    }
    PrintStream stdoutWriter() {
        return stdoutWriter;
    }

    /**
     * Redefine {@link PrintStream} STDERR goes to.
     */
    public DockerRuleBuilder stderrWriter(PrintStream stderrWriter) {
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
    public DockerRuleBuilder publishAllPorts(boolean publishAllPorts) {
        this.publishAllPorts = publishAllPorts;
        return this;
    }
    boolean publishAllPorts() {
        return publishAllPorts;
    }

    /**
     * Static link.
     * Define (legacy) container link (equaivalent of command-line <code>--link</code> option).
     * Unlike dynamic link (see {@link #link(DockerRule, String)}) requires assigning name to target container.
     * Legacy links works only on docker <code>bridge</code> network.
     * <p>
     * Target container must be started first and <b>because of no guarantees of rule execution
     * order in JUnit suggested solution is to take advantage of JUnit {@link RuleChain}</b>, for example:
     * <pre>
     *     DockerRule db = DockerRule.builder()
     *             .imageName("busybox")
     *             .name("db")
     *             ...
     *
     *     DockerRule web = DockerRule.builder()
     *             .imageName("busybox")
     *             .link("db")
     *             ...
     *
     *     {@literal @}Rule
     *     public RuleChain containers = RuleChain.outerRule(db).around(web);
     *
     * </pre>
     *
     * @param link Link definition. Allowed forms are "container" or "container:alias" where
     *             <ul>
     *             <li><code>container</code> is target container name</li>
     *             <li><code>alias</code> alias under which target container will be available in source container</li>
     *             </ul>
     */
    public DockerRuleBuilder link(String link) {
        staticLinks.add(LinkNameValidator.validateContainerLink(link));
        return this;
    }
    List<String> staticLinks() {
        return staticLinks;
    }

    /**
     * Dynamic link.
     * Define (legacy) container links (equaivalent of command-line <code>--link "targetContainerId:alias"</code>
     * where targetContainerId will be substituted after target container start).
     * Legacy links works only on docker <code>bridge</code> network.
     * <p>
     * Unlike static link (see {@link #link(String)}) it does not require assigning name to target container
     * so it is especially convenient in setups where multiple concurrent test cases
     * shares single docker server.
     * <p>
     * Target container must be started first and <b>because of no guarantees of rule execution
     * order in JUnit suggested solution is to take advantage of JUnit {@link RuleChain}</b>, for example:
     * <pre>
     *     DockerRule db = DockerRule.builder()
     *             .imageName("busybox")
     *             ...
     *
     *     DockerRule web = DockerRule.builder()
     *             .imageName("busybox")
     *             .link(db, "db")
     *             ...
     *
     *     {@literal @}Rule
     *     public RuleChain containers = RuleChain.outerRule(db).around(web);
     *
     * </pre>
     *
     * @param targetContainer Container link points to
     * @param alias Alias assinged to link in current container
     *
     */
    public DockerRuleBuilder link(DockerRule targetContainer, String alias) {
        LinkNameValidator.validateContainerName(alias);
        dynamicLinks.add(Pair.of(targetContainer, alias));
        return this;
    }

    List<Pair<DockerRule, String>> getDynamicLinks() {
        return dynamicLinks;
    }

    /**
     * Define container name (equaivalent of command-line <code>--name</code> option).
     */
    public DockerRuleBuilder name(String name) {
        this.name = LinkNameValidator.validateContainerName(name);
        return this;
    }
    String name() {
        return name;
    }

    /**
     * Wait for TCP port listening under given internal container port.
     * Given port MUST be exposed (with {@link #expose(String, String)} or
     * {@link #publishAllPorts(boolean)}) (reachable from the test
     * code point of view).
     * <p>
     * Side note: Internal port is required for convenience - rule will find matching
     * external port or, report error at startup when given internal port was not exposed.
     *
     * @param internalTcpPort TCP port to scan (internal, MUST be exposed for wait to work).
     */
    public DockerRuleBuilder waitForTcpPort(int internalTcpPort) {
        this.waitForPort.add(internalTcpPort);
        return this;
    }
    List<Integer> waitForTcpPort() {
        return waitForPort;
    }

    /**
     * Wait for http endpoint availability under given <b>internal</b> container port.
     * Given port MUST be exposed (with {@link #expose(String, String)} or
     * {@link #publishAllPorts(boolean)}) (reachable from the test
     * code point of view).
     * <p>
     * Side note: Internal port is required for convenience - rule will find matching
     * external port or, report error at startup when given internal port was not exposed.
     *
     * @param internalHttpPort Http port to scan for availability. Port is scanned with HTTP HEAD method
     *                 until response with error code 2xx or 3xx is returned or until timeout.
     *                 Port MUST be exposed for wait to work and given port number must
     *                 be internal (as seen on container, not as on host) port number.
     */
    public DockerRuleBuilder waitForHttpPing(int internalHttpPort) {
        waitForHttp.add(new Integer(internalHttpPort));
        return this;
    }
    List<Integer> waitForHttpPing() {
        return waitForHttp;
    }

    /**
     * Set default timeout for all wait methods.
     *
     * @param waitForSeconds
     */
    public DockerRuleBuilder waitForTimeout(int waitForSeconds) {
        this.waitForSeconds = waitForSeconds;
        return this;
    }

    /**
     * Container stopping behavior. By default container are stopped
     * ({@link StopOption#STOP}) and removed ({@link StopOption#REMOVE}).
     * In case multiple opposite options are given <i>last one wins</i>.
     *
     * @param stopOptions Selected options.
     */
    public DockerRuleBuilder stopOptions(StopOption... stopOptions) {
        this.stopOptions.setOptions(stopOptions);
        return this;
    }
    StopOption.StopOptionSet stopOptions() {
        return this.stopOptions;
    }

    /**
     * Add container label (call multiple times to add more than one).
     * @param name Label name.
     * @param value Label value.
     */
    public DockerRuleBuilder addLabel(String name, String value) {
        labels.put(name, value);
        return this;
    }
    Map<String, String> getLabels() {
        return labels;
    }
}