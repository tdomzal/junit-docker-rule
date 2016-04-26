package pl.domzal.junit.docker.rule;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.ex.InvalidVolumeFrom;

class DockerRuleMountBuilder implements DockerRuleMountToBuilder {

    private static Logger log = LoggerFactory.getLogger(DockerRuleMountBuilder.class);

    private static final String OSX_HOME = "/Users";
    private static final String WIN_HOME_WIN_STYLE = "C:\\Users";
    private static final String WIN_HOME_UNIX_STYLE = "/c/Users";

    private DockerRuleBuilder parentBuilder;

    private String from;
    private String to;
    private String mode;

    DockerRuleMountBuilder(DockerRuleBuilder parentBuilder, String fromPathUnixStyle) throws InvalidVolumeFrom {
        this.parentBuilder = parentBuilder;
        this.from = assertValidMountFrom(SystemUtils.getUserHome().getAbsolutePath(), fromPathUnixStyle, SystemUtils.IS_OS_MAC_OSX, SystemUtils.IS_OS_WINDOWS);
    }

    public DockerRuleBuilder to(String toPath, String mode) {
        this.to = toPath;
        this.mode = mode;
        return parentBuilder.addBind(toString());
    }

    public DockerRuleBuilder to(String toPath) {
        this.to = toPath;
        return parentBuilder.addBind(toString());
    }

    public String toString() {
        return from+":"+to+(StringUtils.isNotEmpty(mode)?":"+mode:"");
    }

    static String assertValidMountFrom(String userHomedir, String mountFrom, boolean isOsMacOsx, boolean isOsWindows) throws InvalidVolumeFrom {
        if (isOsMacOsx && userHomedir.startsWith(OSX_HOME)) {
            if (mountFrom.startsWith(OSX_HOME)) {
                log.debug("mount from - ok for OSX: {}", mountFrom);
                return mountFrom;
            } else {
                throw new InvalidVolumeFrom(String.format("Attempt to mount volume from '%s'. Only mount from inside '%s' is supported in OSX", mountFrom, OSX_HOME));
            }
        } else if (isOsWindows && userHomedir.startsWith(WIN_HOME_WIN_STYLE)) {
            if (mountFrom.startsWith(WIN_HOME_UNIX_STYLE)) {
                log.debug("mount from - ok for Windows: {}", mountFrom);
                return mountFrom;
            } else {
                if (StringUtils.isNotEmpty(mountFrom) && ! StringUtils.startsWith(mountFrom, "/")) {
                    throw new InvalidVolumeFrom(String.format("Attempt to mount volume from '%s'. Paths in Windows must be specified Unix style for example: '/c/Users/..'", mountFrom));
                }
                throw new InvalidVolumeFrom(String.format("Attempt to mount volume from '%s'. Only mount from inside '%s' is supported in Windows.", mountFrom, WIN_HOME_UNIX_STYLE));
            }
        } else {
            log.debug("mount from - assuming Unix: {}", mountFrom);
            return mountFrom;
        }

    }

    /**
     * Convert any absolute path (no matter Unix or Windows style) to Unix style
     * path compatible to docker volume mount from syntax.
     */
    static String toUnixStylePath(String absolutePath) {
        if (StringUtils.isEmpty(absolutePath)) {
            throw new IllegalStateException("empty path given");
        }
        if (absolutePath.startsWith("/")) {
            return absolutePath;
        } else if (absolutePath.length()>=2 && Character.isLetter(absolutePath.charAt(0)) && ':' == absolutePath.charAt(1) && '\\' == absolutePath.charAt(2)) {
            char driveLetter = Character.toLowerCase(absolutePath.charAt(0));
            String drivePath = StringUtils.substringAfter(absolutePath, ":");
            String drivePathUnixStyle = drivePath.replace('\\', '/');
            return "/"+driveLetter+drivePathUnixStyle;
        } else {
            throw new IllegalStateException(String.format("unable to convert path %s", absolutePath));
        }
    }

}