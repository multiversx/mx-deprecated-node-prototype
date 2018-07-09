package network.elrond.data;

import network.elrond.core.CollectionUtil;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockUtil {


    public static List<String> getTransactionsHashesAsString(Block block) {
        List<byte[]> list = getTransactionsHashesAsByte(block);
        return list.stream().
                map(byteHash -> Util.getDataEncoded64(byteHash)).
                collect(Collectors.toList());
    }

    public static List<byte[]> getTransactionsHashesAsByte(Block block) {
        Util.check(block != null, "block != null");

        List<byte[]> list = block.getListTXHashes();
        return list != null ? list : new ArrayList<>();
    }


    public static int getTransactionsCount(Block block) {
        return CollectionUtil.size(getTransactionsHashesAsByte(block));
    }

    public static boolean isEmptyBlock(Block block) {
        return getTransactionsCount(block) == 0;
    }

    public static void addTransactionInBlock(Block block, byte[] transactionHash) {
        block.getListTXHashes().add(transactionHash);
    }

    public static void addTransactionInBlock(Block block, Transaction transaction) {
        block.getListTXHashes().add(AppServiceProvider.getSerializationService().getHash(transaction));
    }
}
