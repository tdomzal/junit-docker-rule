package pl.domzal.junit.docker.rule;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.domzal.junit.docker.rule.ex.InvalidParameter;

@Category(test.category.Stable.class)
public class NameValidatorTest {

    @Test(expected = InvalidParameter.class)
    public void failNull() {
        LinkNameValidator.validateContainerName(null);
    }

    @Test(expected = InvalidParameter.class)
    public void failEmpty() {
        LinkNameValidator.validateContainerName("");
    }

    @Test(expected = InvalidParameter.class)
    public void failInvalidCharsInName1() {
        LinkNameValidator.validateContainerName("host%");
    }

    @Test(expected = InvalidParameter.class)
    public void failInvalidCharsInName2() {
        LinkNameValidator.validateContainerName("host:");
    }

    @Test
    public void okValidName() {
        LinkNameValidator.validateContainerName("abcDEF_-");
    }

}
