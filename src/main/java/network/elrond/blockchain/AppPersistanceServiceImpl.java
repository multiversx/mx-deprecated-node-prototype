package network.elrond.blockchain;

import network.elrond.p2p.P2PConnection;

public class AppPersistanceServiceImpl extends BlockchainServiceImpl {

    @Override
    protected boolean isOffline(P2PConnection connection) {
        return true;
    }
}
