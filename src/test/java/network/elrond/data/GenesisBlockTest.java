package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;
import org.junit.runners.model.TestClass;

public class GenesisBlockTest {
    BlockService blks = AppServiceProvider.getBlockService();

    @Test
    public void testBlock() {
        GenesisBlock gb = new GenesisBlock();
        System.out.println(blks.encodeJSON(gb, true));

    }
}