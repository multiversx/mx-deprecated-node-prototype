package network.elrond.data;

import network.elrond.service.AppServiceProvider;
import org.junit.Test;

public class GenesisBlockTest {

    @Test
    public void testBlock() {
        GenesisBlock gb = new GenesisBlock();
        System.out.println(AppServiceProvider.getSerializationService().encodeJSON(gb));

    }
}