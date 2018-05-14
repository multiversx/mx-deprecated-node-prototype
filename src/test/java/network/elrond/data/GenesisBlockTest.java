package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Test;
import org.junit.runners.model.TestClass;

public class GenesisBlockTest {
    @Test
    public void testBlock() {
        GenesisBlock gb = new GenesisBlock();
        System.out.println(gb.encodeJSON());

    }
}