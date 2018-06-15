package network.elrond;


import junit.framework.TestCase;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.consensus.Validator;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class UtilTest {
    public static void displayListValidators(List<Validator> list) {
        for (int i = 0; i < list.size(); i++) {
            Validator v = list.get(i);

            System.out.println(v.getPubKey() + ", S: " + v.getStake().toString(10) + ", R: " + v.getRating());
        }
        System.out.println();
    }

    @Test
    public void testDisplayStuff1(){
        long currentMillis = System.currentTimeMillis();
        System.out.println(String.format("Event from , time: %1$tY.%1$tm.%1$td %1$tT.%2$03d", new Date(currentMillis), currentMillis % 1000));

        //System.out.println(String.format("%1$03d", 1));
    }

    @Test
    public void testUtilHexToByteArray() {
        TestCase.assertEquals("00", Util.byteArrayToHexString(new byte[]{0}));
        TestCase.assertEquals("0b", Util.byteArrayToHexString(new byte[]{11}));
        TestCase.assertEquals("0f", Util.byteArrayToHexString(new byte[]{15}));
        TestCase.assertEquals("10", Util.byteArrayToHexString(new byte[]{16}));
        TestCase.assertEquals("80", Util.byteArrayToHexString(new byte[]{-128}));
        TestCase.assertEquals("ff", Util.byteArrayToHexString(new byte[]{-1}));
        TestCase.assertEquals("13d18bf84f5643", Util.byteArrayToHexString(new byte[]{19, -47, -117,
        -8, 79, 86, 67}));

        TestCase.assertEquals("13d18bf84f5643", Util.byteArrayToHexString(Util.hexStringToByteArray("13d18bf84f5643")));
    }


    public void testUtilGetAddressFromPublicKey() {
        String strPubKeyHexa = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        String strAddr = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";

        TestCase.assertEquals(strAddr, Util.getAddressFromPublicKey(Util.hexStringToByteArray(strPubKeyHexa)));
    }

    @Test
    public void testUtilGetAddressFromPublicKeyTestVersion() {
        String strPubKeyHexa = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        String strAddr = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";

        TestCase.assertEquals(strAddr, Util.getAddressFromPublicKey(Util.hexStringToByteArray(strPubKeyHexa)));
    }

    public static void createDummyGenesisBlock(Blockchain blockchain){
        Block blockGenesis = new Block();
        blockGenesis.setTimestamp(System.currentTimeMillis() - 1000);

        blockchain.setGenesisBlock(blockGenesis);
    }

    public static void printAccountsWithBalance(Accounts accounts){
        System.out.println("Accounts: ");
        System.out.println("================================================================");

        if (accounts == null){
            System.out.println(" * NULL accounts object!");
            System.out.println("================================================================");
            return;
        }

        if (accounts.getAddresses().size() == 0){
            System.out.println(" * EMPTY set!");
            System.out.println("================================================================");
            return;
        }

        AccountState accountState;

        for (AccountAddress accountAddress : accounts.getAddresses()){

            try {
                accountState = AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts);
            } catch(Exception ex) {
                ex.printStackTrace();
                continue;
            }

            if (accountState == null){
                continue;
            }

            System.out.println(Util.byteArrayToHexString(accountAddress.getBytes()) + ": nonce " +
                    accountState.getNonce().toString(10) + "; balance " +
                    accountState.getBalance().toString(10));
        }



//        for ( entry: nodes.keySet()){
//
//        }
        System.out.println("================================================================");

    }
}
