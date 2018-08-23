package network.elrond.api;

import javafx.util.Pair;
import network.elrond.core.ResponseObject;
import network.elrond.core.Util;
import network.elrond.crypto.PKSKPair;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapType;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.ShardingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CommandLinesInterpretor {

    private static final Logger logger = LogManager.getLogger(CommandLinesInterpretor.class);

    public static ResponseObject interpretCommandLines(String[] args){

        if (args == null){
            return logger.traceExit(new ResponseObject(true, "null arguments!", null));
        }

        if (args.length == 0){
            return logger.traceExit(new ResponseObject(true, "empty arguments", null));
        }

        boolean isHelp = false;
        String configFileName = null;
        String generateFor = "";

        for (int i = 0; i < args.length; i++){
            isHelp = isHelp || isHelpArgument(args[i]);

            if (isConfig(args[i])){
                configFileName = args[i].substring(args[i].indexOf("=") + 1);
            }

            if(isGenerate(args[i])){
                generateFor = args[i].substring(args[i].indexOf("=") + 1);
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

            if (properties == null) {
                System.out.println("Error parsing file! Can not start!");
                return logger.traceExit(new ResponseObject(false, "config file parse error", null));
            }

            if(generateFor == null || generateFor.isEmpty()){

                return logger.traceExit(new ResponseObject(true, "autostart", properties));
            }
            else{

                String[] generateSplit = generateFor.split(";");
                if(generateSplit.length !=3){
                    System.out.println("Error generating! Can not start!");
                    return logger.traceExit(new ResponseObject(false, "Error generating!", null));
                }

                Integer nrShards = Integer.parseInt(generateSplit[0]);
                Integer nodesPerShard = Integer.parseInt(generateSplit[1]);

                Boolean shouldStartAutomatically = Boolean.parseBoolean(generateSplit[2]);

                Map<Integer, List<String>> shardPrivateKeys = new HashMap<>();

                GeneratePrivateKeysPerShard(nrShards, nodesPerShard, shardPrivateKeys);

                List<String> configs = GenerateConfigFiles(shardPrivateKeys);

                String fileName = GenerateCommandFile(configs);

                if(shouldStartAutomatically) {
                    try {
                        Runtime.getRuntime().exec("cmd /c start " + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return logger.traceExit(new ResponseObject(false, "generate", properties));
            }
        }
    }

    private static String GenerateCommandFile(List<String> configs) {
        try {
            int count = 0;
            List<String> commands = new ArrayList<>();
            for (String configFile : configs) {
                count++;
                List<String> lines = Collections.singletonList(
                        String.format("java -jar elrond-api-1.0-SNAPSHOT.jar -config=%s --server.port=%d", configFile, 8080 + count));
                String fileName = String.format("startPeerNode%d.bat",count);
                Path file = Paths.get(fileName);
                Files.write(file, lines, Charset.forName("UTF-8"));

                commands.add("start cmd /k call " + fileName);
            }

            String fileName = "startMultipleGenerated.bat";
            Path file = Paths.get(fileName);
            Files.write(file, commands, Charset.forName("UTF-8"));
            return fileName;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<String> GenerateConfigFiles(Map<Integer,List<String>> shardPrivateKeys) {
       List<String> configs = new ArrayList<>();
        try {
            int count = 0;
        for(int i : shardPrivateKeys.keySet()){
            for(String pKey : shardPrivateKeys.get(i)){
                count++;
                List<String> lines = Arrays.asList(
                 "node_name=elrond-node-" + count,
                "port=" + (4000+count),
                "master_peer_port=31201",
                "peer_ip=127.0.0.1",
                "node_private_key=" + pKey,
                "startup_type=START_FROM_SCRATCH",
                "blockchain_path=elrond-node-" + count,
                "blockchain_restore_path=elrond-node-" + count,
                "is_seeder=false");
                String configFile = "configGen_" + count +".config";
                Path file = Paths.get(configFile);
                    Files.write(file, lines, Charset.forName("UTF-8"));
                    configs.add(configFile);
            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  configs;
    }

    public static void GeneratePrivateKeysPerShard(Integer nrShards, Integer nodesPerShard, Map<Integer, List<String>> shardPrivateKeys) {
        ElrondApiNode node = new ElrondApiNode();
        String[] publicPrivateKeysList = new String[nrShards*nodesPerShard];
        for(int i = 0;i<nrShards;i++){
            List<String> privateKeysList = new ArrayList<>();
            for(int j = 0; j<nodesPerShard ;j++){
                int shardNumber = -1;
                PKSKPair pair;
               do{
                   ResponseObject ro = node.generatePublicKeyAndPrivateKey("");
                   pair = (PKSKPair) ro.getPayload();
                   ShardingService shardingService = AppServiceProvider.getShardingService();
                   byte[] publicKeyBytes = Util.hexStringToByteArray(pair.getPublicKey());
                   shardNumber = shardingService.getShard(publicKeyBytes).getIndex();
               }while(shardNumber!=i);
               privateKeysList.add(pair.getPrivateKey());
               publicPrivateKeysList[j*nodesPerShard + i]  = String.format("Shard: %d  Private: %s / Public: %s", i, pair.getPrivateKey(), pair.getPublicKey());
            }
            shardPrivateKeys.put(i, privateKeysList);
        }

        Path file = Paths.get("generatedKeys.txt");
        try {
            Files.write(file, Arrays.asList(publicPrivateKeysList), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
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

    static boolean isGenerate(String arg){
        if (!isKeyValuePair(arg)){
            return(false);
        }

        String[] split = arg.split("=");
        split[0] = split[0].toUpperCase();

        return split[0].equals("-GENERATE") || split[0].equals("--GENERATE");

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
        System.out.println(" -config=configfile.cfg -generate=nrShards;nodesInShard;autoStart:  Loads the config file from disk and generates scripts for nrShards x nodesInShardInstances");
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
        System.out.println("is_seeder=false");
        System.out.println();
        System.out.println("Variants for above config lines:");
        System.out.println("--------------------------------");
        System.out.println("node_name=AUTO    => will generate an unique name in format elrond-node-" + UUID.randomUUID().toString());
        System.out.println("node_private_key=AUTO    => will generate an unique private key");
        System.out.println("startup_type=" + Arrays.stream(BootstrapType.values()).
                map(Enum::toString).
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
            if (privateKey.toUpperCase().startsWith("REQUESTFROMSEED")){
                try {
                    String uri = "http://"+ipAddress+":"+privateKey.toUpperCase().split(":")[1]+"/node/getNextPrivateKey";
                    logger.info("Requesting from " + uri);
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONObject obj = new JSONObject(content.toString());
                    String pageName = obj.getString("payload");

                    privateKey = pageName.split(";")[1];
                    logger.info("Got PK: " + privateKey);
                }catch (Exception ex){
                    logger.info("Failed to get privateKey " + ex.getMessage()  );
                }
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

            data.put("is_seeder", Boolean.valueOf(properties.get("is_seeder").toString()));

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
