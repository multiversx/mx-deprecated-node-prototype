package network.elrond.api;

import javafx.util.Pair;
import network.elrond.core.ResponseObject;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class CommandLinesInterpretor {

    private static final Logger logger = LogManager.getLogger(CommandLinesInterpretor.class);

    public static ResponseObject interpretCommandLines(String[] args){
        logger.traceEntry("params: {}", args);

        if (args == null){
            return logger.traceExit(new ResponseObject(true, "null arguments!", null));
        }

        if (args.length == 0){
            return logger.traceExit(new ResponseObject(true, "empty arguments", null));
        }

        boolean isHelp = false;
        String configFileName = null;

        for (int i = 0; i < args.length; i++){
            isHelp = isHelp || isHelpArgument(args[i]);

            if (isConfig(args[i])){
                configFileName = args[i].substring(args[i].indexOf("=") + 1);
            }
        }

        if (isHelp){
            displayHelp();
            return logger.traceExit(new ResponseObject(false, "displayed help", null));
        }

        if (configFileName == null){
            return logger.traceExit(new ResponseObject(true, "no file specified", null));
        } else {
            //read config and parse the properties
            Map<String, Object> properties = parseFileProperties(configFileName);

            if (properties == null){
                System.out.println("Error parsing file! Can not start!");
                return logger.traceExit(new ResponseObject(false, "config file parse error", null));
            }

            return logger.traceExit(new ResponseObject(true, "autostart", properties));
        }
    }

    static boolean isHelpArgument(String arg){
        if (arg == null){
            return false;
        }

        arg = arg.toUpperCase();

        return arg.equals("-H") || arg.equals("--H") || arg.equals("-HELP") || arg.equals("--HELP");
    }

    static boolean isConfig(String arg){
        if (!isKeyValuePair(arg)){
            return(false);
        }

        String[] split = arg.split("=");
        split[0] = split[0].toUpperCase();

        return split[0].equals("-CONFIG") || split[0].equals("--CONFIG");

    }

    static boolean isKeyValuePair(String arg){
        if (arg == null){
            return false;
        }

        String[] split = arg.split("=");

        if (split.length != 2){
            return false;
        }

        return split[0].startsWith("-") || split[0].startsWith("--");
    }

    static void displayHelp(){
        System.out.println("ElrondApiApplication");
        System.out.println("============================================================================================");
        System.out.println(" usage:");
        System.out.println(" [no arguments] :  Starts the node in default configuration");
        System.out.println("                   access http://127.0.0.1:8080/swagger-ui.html to start the node OR");
        System.out.println("                   start the UI app");
        System.out.println(" -h --h -H --H -help -HELP --help --HELP :  Display this help message");
        System.out.println(" -config=configfile.cfg :  Loads the config file from disk and automatically starts the node");
        System.out.println();
        System.out.println("Sample of a config file:");
        System.out.println("------------------------");
        System.out.println("node_name=elrond-node-1");
        System.out.println("port=4001");
        System.out.println("master_peer_port=4000");
        System.out.println("peer_ip=127.0.0.1");
        System.out.println("node_private_key=00e073884464d8d887568d6e4e66344db01334436c817bce17653eaf3e428b6ef5");
        System.out.println("startup_type=START_FROM_SCRATCH");
        System.out.println("blockchain_path=elrond-node-1");
        System.out.println("blockchain_restore_path=elrond-node-1");
        System.out.println();
        System.out.println("Variants for above config lines:");
        System.out.println("--------------------------------");
        System.out.println("node_name=AUTO    => will generate an unique name in format elrond-node-" + UUID.randomUUID().toString());
        System.out.println("node_private_key=AUTO    => will generate an unique private key");
        System.out.println("startup_type=" + Arrays.stream(BootstrapType.values()).
                map(val -> val.toString()).
                collect(Collectors.toList()));
        System.out.println("blockchain_path=AUTO    => will get the node's name");
        System.out.println("blockchain_restore_path=AUTO    => will get the node's name");
    }

    static Map<String, Object> parseFileProperties(String configFileName){
        Map<String, Object> data = new HashMap<>();

        List<Pair<String, Object>> values = new ArrayList<>();

        Properties properties = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFileName);
            properties = new Properties();
            properties.load(fileReader);

            String nodeName = (String)properties.get("node_name");
            if (nodeName == null){
                throw new IllegalArgumentException("node_name value is missing!");
            }
            if (nodeName.toUpperCase().equals("AUTO")){
                nodeName = "elrond-node-" + UUID.randomUUID().toString();
            }
            data.put("node_name", nodeName);

            data.put("port", Integer.valueOf(properties.get("port").toString()));
            data.put("master_peer_port", Integer.valueOf(properties.get("master_peer_port").toString()));

            String ipAddress = (String)properties.get("peer_ip");
            if (ipAddress == null){
                throw new IllegalArgumentException("peer_ip value is missing!");
            }
            data.put("peer_ip", ipAddress);

            String privateKey = (String)properties.get("node_private_key");
            if (privateKey == null){
                throw new IllegalArgumentException("private_key value is missing!");
            }
            if (privateKey.toUpperCase().equals("AUTO")){
                privateKey = Util.byteArrayToHexString(new PrivateKey().getValue());
            }
            data.put("node_private_key", privateKey);

            String strBootstrapType = (String)properties.get("startup_type");
            BootstrapType bootstrapType = null;
            if (strBootstrapType == null){
                throw new IllegalArgumentException("startup_type value is missing!");
            }

            for (BootstrapType bType : BootstrapType.values()){
                if (bType.toString().equals(strBootstrapType)){
                    bootstrapType = bType;
                    break;
                }
            }

            if (bootstrapType == null){
                throw new IllegalArgumentException("startup_type is invalid!");
            }
            data.put("startup_type", bootstrapType);

            String blockchainPath = (String)properties.get("blockchain_path");
            if (blockchainPath == null){
                throw new IllegalArgumentException("blockchainPath value is missing!");
            }
            if (blockchainPath.toUpperCase().equals("AUTO")){
                blockchainPath = nodeName;
            }
            data.put("blockchain_path", blockchainPath);

            String blockchainRestorePath = (String)properties.get("blockchain_restore_path");
            if (blockchainRestorePath == null){
                throw new IllegalArgumentException("blockchainRestorePath value is missing!");
            }
            if (blockchainRestorePath.toUpperCase().equals("AUTO")){
                blockchainRestorePath = nodeName;
            }
            data.put("blockchain_restore_path", blockchainRestorePath);

            return(data);
        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Error loading config file!");
            return(null);
        } finally {
            try{
                fileReader.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
