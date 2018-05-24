package network.elrond.blockchain;

import network.elrond.p2p.P2PConnection;

public class AppPersistenceServiceImpl extends BlockchainServiceImpl implements AppPersistenceService {

    @Override
    protected boolean isOffline(P2PConnection connection) {
        return true;
    }
}
