package network.elrond.processor.impl.executor;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import network.elrond.Application;
import network.elrond.AsciiTable;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class StatusPrinterBlockTask implements AppTask {
    private static final Logger logger = LogManager.getLogger(StatusPrinterBlockTask.class);

    @Override
	public void process(Application application) {
        AppState state = application.getState();

        Thread thread = new Thread(() -> {
            int waitError = 1000;
            int waitNormal = 60000;


            while (state.isStillRunning()) {
                P2PConnection connection = state.getConnection();
                if (connection == null){
                    ThreadUtil.sleep(waitError);
                    continue;
                }

                Peer peer = connection.getPeer();
                if (peer == null){
                    ThreadUtil.sleep(waitError);
                    continue;
                }

                List<PeerAddress> peerAddresses = peer.peerBean().peerMap().all();
                if (peerAddresses == null){
                    ThreadUtil.sleep(waitError);
                    continue;
                }

                logger.info("\r\n" + printPeers(peerAddresses).render());
                logger.info("\r\n" + printBucket(connection.getAllPeers()).render());

                ThreadUtil.sleep(waitNormal);
            }
        });
        thread.start();
    }

    private AsciiTable printPeers(List<PeerAddress> peerAddressesList){
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(200);

        table.getColumns().add(new AsciiTable.Column("Verified peers: "));

        for (PeerAddress peerAddress : peerAddressesList) {
            AsciiTable.Row row = new AsciiTable.Row();
            row.getValues().add(peerAddress.toString());
            table.getData().add(row);
        }

        table.calculateColumnWidth();
        return table;
    }

    private AsciiTable printBucket(Map<Integer, HashSet<PeerAddress>> hashMap){
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(200);

        table.getColumns().add(new AsciiTable.Column(""));
        table.getColumns().add(new AsciiTable.Column("Shard buckets: "));

        int oldShard = -1;
        for (int shard : hashMap.keySet()){
            HashSet<PeerAddress> peersInBucket = hashMap.get(shard);

            for (PeerAddress peerAddress : peersInBucket) {
                AsciiTable.Row row = new AsciiTable.Row();

                if (oldShard != shard) {
                    if (oldShard != -1) {
                        AsciiTable.Row rowMinus = new AsciiTable.Row();
                        rowMinus.getValues().add("---");
                        rowMinus.getValues().add("--------------------------------------------------------------------------------------------------");
                        table.getData().add(rowMinus);
                    }

                    row.getValues().add(String.valueOf(shard));
                    oldShard = shard;
                } else {
                    row.getValues().add(" ");
                }

                row.getValues().add(peerAddress.toString());
                table.getData().add(row);
            }
        }


        table.calculateColumnWidth();
        return table;
    }


}
