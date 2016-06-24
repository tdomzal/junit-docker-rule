package pl.domzal.junit.docker.rule;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.domzal.junit.docker.rule.ex.InvalidParameter;

@Category(test.category.Stable.class)
public class LinkValidatorTest {

    @Test(expected = InvalidParameter.class)
    public void failNull() {
        LinkNameValidator.validateContainerLink(null);
    }

    @Test(expected = InvalidParameter.class)
    public void failEmpty() {
        LinkNameValidator.validateContainerLink("");
    }

    @Test(expected = InvalidParameter.class)
    public void failInvalidCharsInName() {
        LinkNameValidator.validateContainerLink("host%");
    }

    @Test
    public void okValidName() {
        LinkNameValidator.validateContainerLink("abcDEF_-");
    }

    @Test
    public void okValidNameAndAlias() {
        LinkNameValidator.validateContainerLink("db:some_container");
    }

    @Test(expected = InvalidParameter.class)
    public void failTooManyColons() {
        LinkNameValidator.validateContainerLink("db:some_container:other_container");
    }

}
