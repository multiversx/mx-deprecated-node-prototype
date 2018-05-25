package network.elrond.plaintests;

import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class PlainTest01 {

    public static void main(String[] args)
    {
        String hostName = "127.0.0.1";
        int portNumber = 4096;

        Socket sk;

        try {
            sk = new Socket(hostName, portNumber);


        }
        catch (Exception ex) {
            LoggerFactory.getLogger("elrond.PlainTest01").info(ex.getMessage());
            LoggerFactory.getLogger("PlainTest01").error(ex.getMessage() + " billing");
            LoggerFactory.getLogger("PlainTest01").warn(ex.getMessage());
        }

        System.out.println(Arrays.equals(("aaa").getBytes(), new byte[]{97, 97, 97}));

        final String startupDir = System.getProperty("user.dir");
        Path pathBlk = Paths.get(startupDir, "producer", "blockchain.block.data");

        System.out.println(pathBlk.toString());
        System.out.println(SettingsType.MAX_BLOCK_HEIGHT.toString());

        System.out.println(AppServiceProvider.getSerializationService().encodeJSON("aaaaaBBBB"));
        System.out.println(AppServiceProvider.getSerializationService().encodeJSON(BigInteger.ZERO));
    }

    @Test
    public void getPrivPubKeyTest(){
        PrivateKey privKey = new PrivateKey("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent ultrices velit elit, non bibendum leo suscipit ac.");
        PublicKey pubKey = new PublicKey(privKey);

        System.out.println("Public key: " + Util.getHashEncoded64(pubKey.getValue()) + " address: " + Util.getAddressFromPublicKey(pubKey.getValue()));
    }


}
