package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.benchmark.*;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.*;
import network.elrond.crypto.KeysManager;
import network.elrond.crypto.PKSKPair;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Block;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;
import network.elrond.p2p.*;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ElrondFacadeImpl implements ElrondFacade {
    private static final Logger logger = LogManager.getLogger(ElrondFacadeImpl.class);

    @Override
    public Application start(AppContext context) {
        logger.traceEntry("params: {}", context);

        try {
            Application application = new Application(context);
            application.start();
            logger.trace("Started");
            return logger.traceExit(application);
        } catch (Exception e) {
            logger.catching(e);
            return logger.traceExit((Application) null);
        }
    }

    @Override
    public boolean stop(Application application) {
        logger.traceEntry("params: {}", application);

        try {
            application.stop();
            logger.trace("Stopped");
            return logger.traceExit(true);
        } catch (Exception e) {
            logger.catching(e);
            return logger.traceExit(false);
        }
    }

    @Override
    public ResponseObject getBalance(AccountAddress address, Application application) {
        logger.traceEntry("params: {} {}", address, application);

        if (application == null) {
            logger.warn("application is null");
            return (new ResponseObject(false, "Invalid application state, application is null", null));
        }

        if (address == null) {
            logger.warn("address is null");
            return logger.traceExit(new ResponseObject(false, "Address is null", null));
        }

        AppState state = application.getState();
        Shard currentShard = application.getState().getShard();
        Shard addressShard = AppServiceProvider.getShardingService().getShard(address.getBytes());

        if (ObjectUtil.isEqual(addressShard, currentShard)) {
            try {
                Accounts accounts = state.getAccounts();
                AccountState account = AppServiceProvider.getAccountStateService().getAccountState(address, accounts);

                if (account == null) {
                    return logger.traceExit(new ResponseObject(true, "", BigInteger.ZERO));
                } else {
                    return logger.traceExit(new ResponseObject(true, "", account.getBalance()));
                }
            } catch (Exception ex) {
                logger.throwing(ex);
                return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
            }
        }

        try {

            P2PRequestChannel channel = state.getChannel(P2PRequestChannelName.ACCOUNT);
            AccountState account = AppServiceProvider.getP2PRequestService().get(channel, addressShard, P2PRequestChannelName.ACCOUNT, address);

            if (account == null) {
                return logger.traceExit(new ResponseObject(true, "", BigInteger.ZERO));
            } else {
                return logger.traceExit(new ResponseObject(true, "", account.getBalance()));
            }
        } catch (Exception ex) {
            logger.throwing(ex);
            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject getReceipt(String transactionHash, Application application) {
        logger.traceEntry("params: {} {}", transactionHash, application);

        if (application == null) {
            logger.warn("application is null");
            return (new ResponseObject(false, "Invalid application state, application is null", null));
        }

        if (transactionHash == null) {
            logger.warn("transactionHash is null");
            return (new ResponseObject(false, "Transaction hash is null", null));
        }

        Blockchain blockchain = application.getState().getBlockchain();

        try {

            Receipt receipt = FutureUtil.get(() -> {

                String receiptHash;
                Receipt receiptLocal = null;
                do {
                    receiptHash = AppServiceProvider.getBlockchainService().get(transactionHash, blockchain, BlockchainUnitType.TRANSACTION_RECEIPT, false);
                    if (receiptHash != null) {
                        AppServiceProvider.getBlockchainService().putLocal(transactionHash, receiptHash, blockchain, BlockchainUnitType.TRANSACTION_RECEIPT);
                        receiptLocal = AppServiceProvider.getBlockchainService().get(receiptHash, blockchain, BlockchainUnitType.RECEIPT, false);
                        if (receiptLocal != null) {
                            AppServiceProvider.getBlockchainService().putLocal(receiptHash, receiptLocal, blockchain, BlockchainUnitType.RECEIPT);
                        }
                    }
                    ThreadUtil.sleep(200);

                } while (receiptHash == null || receiptLocal == null);

                return receiptLocal;

            }, 60L);

            return logger.traceExit(new ResponseObject(true, "", receipt));

        } catch (Exception ex) {
            logger.catching(ex);
            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject sendMultipleTransactions(AccountAddress receiver, BigInteger value, Integer noTransactions, Application application) {
        logger.traceEntry("params: {} {} {}", receiver, value, application);

        if (application == null) {
            logger.warn("Invalid application state, application is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, application is null", null));
        }

        if (receiver == null) {
            logger.warn("Receiver is null");
            return logger.traceExit(new ResponseObject(false, "Receiver is null", null));
        }

        if (value == null) {
            logger.warn("Value is null");
            return logger.traceExit(new ResponseObject(false, "Value is null", null));
        }

        if (value.compareTo(BigInteger.ONE) < 0) {
            logger.warn("Value must be greater than 0");
            return logger.traceExit(new ResponseObject(false, "Value must be greater than 0", null));
        }

        if (noTransactions < 1) {
            logger.warn("Number of transactions must be greater than 0");
            return logger.traceExit(new ResponseObject(false, "Number of transactions must be greater than 0", null));
        }

        MultipleTransactionResult result = new MultipleTransactionResult();
        List<Transaction> transactions = new ArrayList<>();
        try {
            for (int i = 0; i < noTransactions; i++) {
                ResponseObject responseObject = generateTransaction(receiver, value, application.getState());

                if (responseObject.isSuccess()) {
                    transactions.add((Transaction) responseObject.getPayload());
                }
            }

            transactions.stream().parallel().filter(Objects::nonNull).forEach((tr) -> {
                try {
                    sendTransaction(application.getState(), tr);
                } catch (IOException e) {
                    logger.catching(e);
                }
            });

            int successful = (int) transactions.stream().filter(Objects::nonNull).count();
            result.setSuccessfulTransactionsNumber(successful);
            result.setFailedTransactionsNumber(noTransactions - successful);

        } catch (Exception e) {
            logger.catching(e);
        }

        return logger.traceExit(new ResponseObject(true, "", result));
    }

    @Override
    public ResponseObject sendMultipleTransactionsToAllShards(BigInteger value, Integer nrTransactions, Application application) {
        boolean success = true;
        String lastError = "";

        port = 8080;
        List<String> currentPeers = KeysManager.getInstance().getConnectedPeers();
        String local = "127.0.0.1;" + application.getState().getShard().getIndex().toString();
        if(!currentPeers.contains(local)) {
            currentPeers.add(0, local);
        }

        currentPeers.parallelStream().forEach( peer -> {

            Runnable myrunnable = new Runnable() {
                public void run() {
                    String[] splitPeer = peer.split(";");
                    URL url = null;
                    try {
                        String address = KeysManager.getInstance().getSendToPeers().get(Integer.parseInt(splitPeer[1])).split(";")[2];

                        url = new URL("http://" + splitPeer[0] + ":" + getport() +"/node/sendMultipleTransactions" +
                                "?address=" + address +
                                "&value=" + value +
                                "&nrTransactions=" + nrTransactions);

                        logger.info(url);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer content = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }

                        logger.info(content.toString());
                    } catch (Exception ex) {
                        logger.throwing(ex);
//                lastError = "Error sending transactions: " + ex.getMessage();
//                success = false;
                    }
                }
            };

            new Thread(myrunnable).start();


        });

        if (!success){
            return new ResponseObject(false, lastError, "Not Successful");
        }

        return new ResponseObject(true, "", "Successful sent transactions to all nodes");
    }
    int port = 0;
    synchronized int getport(){
        return 8080;
        //return port;
    }

    @Override
    public ResponseObject send(AccountAddress receiver, BigInteger value, Application application) {
        logger.traceEntry("params: {} {} {}", receiver, value, application);

        if (application == null) {
            logger.warn("Invalid application state, application is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, application is null", null));
        }

        if (receiver == null) {
            logger.warn("Receiver is null");
            return logger.traceExit(new ResponseObject(false, "Receiver is null", null));
        }

        if (value == null) {
            logger.warn("Value is null");
            return logger.traceExit(new ResponseObject(false, "Value is null", null));
        }

        if (value.compareTo(BigInteger.ONE) < 0) {
            logger.warn("Value must be greater than 0");
            return logger.traceExit(new ResponseObject(false, "Value must be greater than 0", null));
        }

        try {
            ResponseObject responseObjectTransaction = generateTransaction(receiver, value, application.getState());
            if (responseObjectTransaction.isSuccess()) {
                sendTransaction(application.getState(), (Transaction) responseObjectTransaction.getPayload());
            }

            return logger.traceExit(responseObjectTransaction);

        } catch (Exception ex) {
            logger.catching(ex);
            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    private ResponseObject generateTransaction(AccountAddress receiver, BigInteger value, AppState state) throws IOException, ClassNotFoundException {
        Accounts accounts = state.getAccounts();

        PublicKey senderPublicKey = state.getPublicKey();
        PrivateKey senderPrivateKey = state.getPrivateKey();
        AccountAddress senderAddress = AccountAddress.fromBytes(senderPublicKey.getValue());
        AccountState senderAccount = AppServiceProvider.getAccountStateService().getAccountState(senderAddress, accounts);

        if (state == null) {
            logger.warn("Invalid application state, state is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, state is null", null));
        }

        if (senderAccount == null) {
            // sender account is new, can't send
            logger.warn("Sender account is new, can't send");
            return logger.traceExit(new ResponseObject(false, "Sender account is new, can't send", null));
        }

        PublicKey receiverPublicKey = new PublicKey(receiver.getBytes());

        if (!receiverPublicKey.isValid()) {
            // receiver account is invalid
            logger.warn("Receiver account is invalid");
            return logger.traceExit(new ResponseObject(false, "Receiver account is invalid", null));
        }

        BigInteger nonce = senderAccount.getNonce();
        Transaction transaction = AppServiceProvider.getTransactionService().generateTransaction(senderPublicKey, receiverPublicKey, value, nonce);
        AppServiceProvider.getTransactionService().signTransaction(transaction, senderPrivateKey.getValue(), senderPublicKey.getValue());
        if (transaction == null) {
            return logger.traceExit(new ResponseObject(false, "Error generating transaction", transaction));
        } else {
            return logger.traceExit(new ResponseObject(true, "", transaction));
        }
    }

    private void sendTransaction(AppState state, Transaction transaction) throws java.io.IOException {
        //String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
        //P2PConnection connection = state.getConnection();
        //AppServiceProvider.getP2PObjectService().put(connection, hash, transaction);

        P2PBroadcastChannel channel = state.getChannel(P2PBroadcastChannelName.TRANSACTION);
        AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, transaction, state.getShard().getIndex());
    }

    @Override
    public ResponseObject ping(String ipAddress, int port) {
        logger.traceEntry("params: {} {}", ipAddress, port);
        try {
            PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse(ipAddress, port, true);
            return logger.traceExit(new ResponseObject(true, "", pingResponse));
        } catch (Exception ex) {
            logger.catching(ex);

            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject checkFreePort(String ipAddress, int port) {
        logger.traceEntry("params: {} {}", ipAddress, port);
        try {
            PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse(ipAddress, port, false);
            return logger.traceExit(new ResponseObject(true, "", pingResponse));
        } catch (Exception ex) {
            logger.catching(ex);

            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject generatePublicKeyAndPrivateKey(String strPrivateKey) {
        logger.traceEntry("params: {}", strPrivateKey);
        try {
            PrivateKey privateKey = null;

            if ((strPrivateKey == null) || strPrivateKey.isEmpty()) {
                privateKey = new PrivateKey();
            } else {
                privateKey = new PrivateKey(Util.hexStringToByteArray(strPrivateKey));
            }

            PublicKey publicKey = new PublicKey(privateKey);

            return logger.traceExit(new ResponseObject(true, "",
                    new PKSKPair(Util.byteArrayToHexString(publicKey.getValue()), Util.byteArrayToHexString(privateKey.getValue()))));
        } catch (Exception ex) {
            logger.catching(ex);
            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject getBenchmarkResult(String benchmarkId, Application application) {
        logger.traceEntry("params: {} {}", benchmarkId, application);

        if (benchmarkId != null && !benchmarkId.isEmpty()) {
            logger.warn("BenchmarkID is null or empty");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, state is null", null));
        }

        if (application == null) {
            logger.warn("Invalid application state, application is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, application is null", null));
        }

        AppState state = application.getState();

        if (state == null) {
            logger.warn("Invalid application state, state is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, state is null", null));
        }

        P2PRequestChannel channel = state.getChannel(P2PRequestChannelName.STATISTICS);
        Integer numberOfShards = AppServiceProvider.getShardingService().getNumberOfShards();
        Integer numberNodesInNetwork = AppShardingManager.instance().getNumberNodesInNetwork(application.getState());

        ArrayList<StatisticsManager> statisticsManagers = new ArrayList<>();

        int currentShard = state.getShard().getIndex();
        for (int i = 0; i < numberOfShards; i++) {
            if (i == currentShard) {
                statisticsManagers.add(state.getStatisticsManager());
            } else {
                Shard addressShard = new Shard(i);
                StatisticsManager statisticsManager = AppServiceProvider.getP2PRequestService().get(channel, addressShard, P2PRequestChannelName.STATISTICS, null);
                if (statisticsManager != null) {
                    statisticsManagers.add(statisticsManager);
                } else {
                    statisticsManagers.add(new StatisticsManager(new ElrondSystemTimerImpl(), i));
                }
            }
        }

        BenchmarkResult res = BenchmarkManager.getInstance().getBenchmarkResult(statisticsManagers);
        res.setNetworkActiveNodes(numberNodesInNetwork);
        res.setNrShards(numberOfShards);

        return logger.traceExit(new ResponseObject(true, "", res));
    }

    @Override
    public ResponseObject getTransactionFromHash(String transactionHash, Blockchain blockchain) {
        logger.traceEntry("params: {} {}", transactionHash, blockchain);
        try {
            Transaction transaction = AppServiceProvider.getBlockchainService().get(transactionHash, blockchain, BlockchainUnitType.TRANSACTION, false);

            if (transaction == null) {
                return logger.traceExit(new ResponseObject(true, String.format("Transaction with hash %s was not found", transactionHash), null));
            }

            AppServiceProvider.getBlockchainService().putLocal(transactionHash, transaction, blockchain, BlockchainUnitType.TRANSACTION);

            return logger.traceExit(new ResponseObject(true, "", transaction));
        } catch (Exception ex) {
            logger.throwing(ex);
            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject getBlockFromHash(String blockHash, Blockchain blockchain) {
        logger.traceEntry("params: {} {}", blockchain, blockchain);
        try {
            Block block = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK, false);

            if (block == null) {
                return logger.traceExit(new ResponseObject(true, String.format("Block with hash %s was not found", blockHash), null));
            }

            AppServiceProvider.getBlockchainService().putLocal(blockHash, block, blockchain, BlockchainUnitType.BLOCK);

            return logger.traceExit(new ResponseObject(true, "", block));
        } catch (Exception ex) {
            logger.throwing(ex);
            return logger.traceExit(new ResponseObject(false, ex.getMessage(), null));
        }
    }

    @Override
    public ResponseObject getNextPrivateKey(String remoteAddress) {
        return logger.traceExit(new ResponseObject(true, "", KeysManager.getInstance().getNextPrivateKey(remoteAddress) ));
    }

    @Override
    public ResponseObject getPrivatePublicKeyShard(Application application){
        if (application == null){
            return new ResponseObject(false, "Node not started!", null);
        }

        PublicKey publicKey = application.getState().getPublicKey();
        PrivateKey privateKey = application.getContext().getPrivateKey();
        Shard shard = application.getState().getShard();

        if (privateKey == null){
            return new ResponseObject(false, "Error reading data from node! [private key]", null);
        }

        if (publicKey == null){
            return new ResponseObject(false, "Error reading data from node! [public key]", null);
        }

        if (shard == null){
            return new ResponseObject(false, "Error reading data from node! [shard]", null);
        }

        return new ResponseObject(true, "", new String[]{Util.byteArrayToHexString(privateKey.getValue()),
                Util.byteArrayToHexString(publicKey.getValue()), shard.getIndex().toString()});
    }

}
