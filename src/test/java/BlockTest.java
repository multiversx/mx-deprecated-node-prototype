import junit.framework.TestCase;
import network.elrond.data.Block;
import org.junit.Test;

public class BlockTest {
    @Test
    public void testBlock() {
        Block b1 = new Block("Hello world from Elrond", "0");

        String data = b1.getData();
        TestCase.assertEquals("Hello world from Elrond", b1.getData());

        String hash = b1.getHash();
        TestCase.assertEquals("?", b1.getHash());
    }
}
