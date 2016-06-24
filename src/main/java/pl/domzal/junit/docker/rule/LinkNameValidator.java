package pl.domzal.junit.docker.rule;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import pl.domzal.junit.docker.rule.ex.InvalidParameter;

class LinkNameValidator {

    private static final String VALID_NAME_REGEX = "[a-zA-Z0-9_-]+";
    private static final String VALID_LINK_DEFINITION_REGEX = "[a-zA-Z0-9_-]+(:[a-zA-Z0-9_-]+)?";

    private static Pattern NAME_PATTERN = Pattern.compile(VALID_NAME_REGEX);
    private static Pattern LINK_PATTERN = Pattern.compile(VALID_LINK_DEFINITION_REGEX);

    public static String validateContainerName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new InvalidParameter("Name definition is empty");
        }
        if (NAME_PATTERN.matcher(name).matches()) {
            return name;
        } else {
            throw new InvalidParameter(String.format("'%s' is not valid name", name));
        }
    }

    public static String validateContainerLink(String link) {
        if (StringUtils.isBlank(link)) {
            throw new InvalidParameter("Link definition is empty");
        }
        if (LINK_PATTERN.matcher(link).matches()) {
            return link;
        } else {
            throw new InvalidParameter(String.format("'%s' is not valid link definition", link));
        }
    }
}
