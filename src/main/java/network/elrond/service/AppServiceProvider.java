package network.elrond.service;

import network.elrond.p2p.P2PBroadcastService;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import network.elrond.p2p.P2PObjectService;
import network.elrond.p2p.P2PObjectServiceImpl;

public class AppServiceProvider {


    private static P2PBroadcastService p2PBroadcastService = new P2PBroadcastServiceImpl();

    public static P2PBroadcastService getP2PBroadcastService() {
        return p2PBroadcastService;
    }


    private static P2PObjectService p2PObjectService = new P2PObjectServiceImpl();

    public static P2PObjectService getP2PObjectService() {
        return p2PObjectService;
    }


}
