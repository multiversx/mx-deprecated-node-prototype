package network.elrond;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class ExpectedExceptionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected <H> void expected(Class<? extends Exception> expectedExceptionClass, String expectedMessage) {
        exception.expect(expectedExceptionClass);
        exception.expectMessage(expectedMessage);
    }
}
