package network.elrond.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class KeysManager {
    private static final Logger logger = LogManager.getLogger(KeysManager.class);
    private static KeysManager instance;

    private List<String> privateKeys;
    private List<String> connectedPeers = new ArrayList<>();
    private List<String> sendToPeers = new ArrayList<>();

    public static KeysManager getInstance() {
        if(instance == null){
            instance = new KeysManager();
        }

        return instance;
    }

    private KeysManager(){
        logger.info("Starting Keysmanager");
        Path kez = Paths.get("kez.txt");

        try {
            privateKeys = Files.readAllLines(kez);
            logger.info("Read a number kez of " + privateKeys.size());
        } catch (IOException e) {
            logger.info("Cannot read kez.txt. " + e.getMessage());
        }

        Path sendToKez = Paths.get("sendToKez.txt");

        try {
            sendToPeers = Files.readAllLines(sendToKez);
            logger.info("Read a number sendToKez of " + privateKeys.size());
        } catch (IOException e) {
            logger.info("Cannot read sendToKez.txt. " + e.getMessage());
        }
    }

    public String getNextPrivateKey(String remoteAddress) {
        if(privateKeys.size() > 0){
            String pk = privateKeys.get(0);
            getConnectedPeers().add(remoteAddress +";"+pk);
            Path file = Paths.get("connectedPeers.txt");
            try {
                Files.write(file, getConnectedPeers(), Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            privateKeys.remove(0);
            return  pk;
        }
        return null;
    }

    public List<String> getConnectedPeers() {
        return connectedPeers;
    }

    public List<String> getSendToPeers() {
        return sendToPeers;
    }
}
