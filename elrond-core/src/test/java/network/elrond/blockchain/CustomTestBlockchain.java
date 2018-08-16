package network.elrond.blockchain;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class CustomTestBlockchain extends Blockchain {
    public CustomTestBlockchain(BlockchainContext context) throws IOException {
        super(context);
    }

    @Override
    public void generatePersistenceUnitMap(BlockchainContext context) {
        for (BlockchainUnitType type : BlockchainUnitType.values()) {
            String path = context.getDatabasePath(type);
            Class<?> ketType = type.getKeyType();
            Class<?> valueType = type.getValueType();
            BlockchainPersistenceUnit<?, ?> unit = mock(BlockchainPersistenceUnit.class);
            blockchain.put(type, unit);
        }

    }
}
