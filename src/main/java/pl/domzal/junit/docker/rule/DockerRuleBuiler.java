package pl.domzal.junit.docker.rule;

import org.apache.commons.lang.StringUtils;

public class DockerRuleBuiler {

    private String imageName;
    private String[] cmd;
    private String[] exposedPorts;
    private String[] extraHosts;
    private String waitForMessage;
    private boolean keepContainer = false;
    private boolean imageAlwaysPull = false;

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
    public DockerRuleBuiler setCmd(String... cmd) {
        this.cmd = cmd;
        return this;
    }
    public String[] getCmd() {
        return nullToEmpty(cmd);
    }

    /**
     * Image name to be used (required).
     */
    public DockerRuleBuiler setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }
    public String getImageName() {
        if (StringUtils.isEmpty(imageName)) {
            throw new IllegalStateException("imageName cannot be empty");
        }
        return imageName;
    }

    /**
     * Container ports to expose on host.
     * Port specific container port was mapped to can be later retreived with {@link DockerRule#getExposedContainerPort(String)}.
     */
    public DockerRuleBuiler setExposedPorts(String... exposedPorts) {
        this.exposedPorts = exposedPorts;
        return this;
    }
    public String[] getExposedPorts() {
        return nullToEmpty(exposedPorts);
    }

    /**
     * Add extra host definitions into containers <code>/etc/hosts</code>.
     * @param extraHosts List of host matching format "hostname:address" (like desribed for 'docker run --add-host').
     */
    public DockerRuleBuiler setExtraHosts(String... extraHosts) {
        this.extraHosts = extraHosts;
        return this;
    }
    public String[] getExtraHosts() {
        return nullToEmpty(extraHosts);
    }

    /**
     * Make rule to wait for specified text in log on container start.
     */
    public DockerRuleBuiler setWaitForMessage(String waitForMessage) {
        this.waitForMessage = waitForMessage;
        return this;
    }
    public String getWaitForMessage() {
        return waitForMessage;
    }

    /**
     * Keep stopped container after test.
     */
    public DockerRuleBuiler setKeepContainer(boolean keepContainer) {
        this.keepContainer = keepContainer;
        return this;
    }
    public boolean getKeepContainer() {
        return keepContainer;
    }

    /**
     * Force image pull even when image is already present.
     */
    public DockerRuleBuiler setImageAlwaysPull(boolean alwaysPull) {
        this.imageAlwaysPull = alwaysPull;
        return this;
    }
    public boolean getImageAlwaysPull() {
        return imageAlwaysPull;
    }
}