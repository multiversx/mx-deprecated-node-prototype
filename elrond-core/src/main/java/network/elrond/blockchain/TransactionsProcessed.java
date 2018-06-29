package network.elrond.blockchain;

import network.elrond.core.Util;
import network.elrond.data.Block;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransactionsProcessed {
    protected Map<BigInteger, List<String>> transactionsProcessed;

    public static final int WINDOW_SIZE = 5;

    public TransactionsProcessed(){
        transactionsProcessed = new ConcurrentHashMap<>();
    }

    public boolean checkExists(String transactionHash){
        Util.check(transactionHash != null, "transactionHash != null");

        for (List<String> hashes : transactionsProcessed.values()){
            if (hashes.contains(transactionHash)){
                return(true);
            }
        }

        return(false);
    }

    public void addBlock(Block block){
        Util.check(block != null, "block != null");

        List<byte[]> byteHashes = block.getListTXHashes();

        if (byteHashes.size() == 0){
            return;
        }

        List<BigInteger> nonces = transactionsProcessed.keySet().stream().sorted().collect(Collectors.toList());

        if (nonces.contains(block.getNonce())){
            return;
        }

        List<String> hashes = byteHashes.stream().
                map(byteHash -> Util.getDataEncoded64(byteHash)).
                collect(Collectors.toList());

        transactionsProcessed.put(block.getNonce(), hashes);

        if (nonces.size() + 1 > WINDOW_SIZE){
            transactionsProcessed.remove(nonces.get(0));
        }
    }

}
