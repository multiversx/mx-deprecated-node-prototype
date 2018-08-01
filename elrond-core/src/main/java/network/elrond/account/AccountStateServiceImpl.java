package network.elrond.account;

import net.tomp2p.peers.PeerAddress;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.chronology.NTPClient;
import network.elrond.core.RLP;
import network.elrond.core.RLPList;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastChannel;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccountStateServiceImpl implements AccountStateService {
    private static final Logger logger = LogManager.getLogger(AccountStateServiceImpl.class);

    @Override
    public synchronized AccountState getOrCreateAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException {
        logger.traceEntry("params: {} {}", address, accounts);

        Util.check(address != null, "address!=null");
        Util.check(accounts != null, "accounts!=null");

        AccountState state = getAccountState(address, accounts);

        if (state != null) {
            logger.trace("state not null");
            return logger.traceExit(state);
        }

        logger.trace("Create account state...");
        setAccountState(address, new AccountState(address), accounts);
        return logger.traceExit(getAccountState(address, accounts));
    }

    @Override
    public synchronized AccountState getAccountState(AccountAddress address, Accounts accounts) {
        logger.traceEntry("params: {} {}", address, accounts);
        if (address == null) {
            logger.trace("address is null");
            return logger.traceExit((AccountState) null);
        }

        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        byte[] bytes = address.getBytes();
        byte[] data = unit.get(bytes);

        return logger.traceExit((data != null) ? convertToAccountStateFromRLP(data) : null);
    }

    @Override
    public synchronized void rollbackAccountStates(Accounts accounts) {
        logger.traceEntry("params: {}", accounts);
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.rollBack();
        logger.traceExit();
    }

    @Override
    public synchronized void commitAccountStates(Accounts accounts) {
        logger.traceEntry("params: {}", accounts);
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.commit();
        logger.traceExit();
    }

    @Override
    public synchronized void setAccountState(AccountAddress address, AccountState state, Accounts accounts) {
        logger.traceEntry("params: {} {} {}", address, state, accounts);
        if (address == null || state == null) {
            logger.trace("address or state is null");
            logger.traceExit();
            return;
        }

        logger.trace("Setting account state...");
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.put(address.getBytes(), convertAccountStateToRLP(state));
        unit.getCache().put(address, state);
        accounts.getAddresses().add(address);
        logger.traceExit();
    }

    @Override
    public byte[] convertAccountStateToRLP(AccountState accountState) {
        logger.traceEntry("params: {}", accountState);
        byte[] nonce = RLP.encodeBigInteger(accountState.getNonce());
        byte[] balance = RLP.encodeBigInteger(accountState.getBalance());
        byte[] address = RLP.encodeElement(accountState.getAddress().getBytes());

        return logger.traceExit(RLP.encodeList(nonce, balance, address));
    }


    @Override
    public AccountState convertToAccountStateFromRLP(byte[] data) {
        logger.traceEntry("params: {}", data);
        if (data == null || data.length == 0) {
            logger.trace("data is null or data.length = 0");
            return logger.traceExit((AccountState) null);
        }

        byte[] EMPTY_DATA = {0};
        RLPList items = (RLPList) RLP.decode2(data).get(0);

        byte[] nonceRlpData = items.get(0).getRLPData();
        BigInteger nonce = new BigInteger(1, (nonceRlpData == null ? EMPTY_DATA : nonceRlpData));


        byte[] balanceRlpData = items.get(1).getRLPData();
        BigInteger balance = new BigInteger(1, (balanceRlpData == null ? EMPTY_DATA : balanceRlpData));


        byte[] addressRlpData = items.get(2).getRLPData();
        AccountAddress address = AccountAddress.fromBytes((addressRlpData == null ? EMPTY_DATA : addressRlpData));


        AccountState accountState = new AccountState(address);
        accountState.setNonce(nonce);
        accountState.setBalance(balance);


        return logger.traceExit(accountState);
    }

    public void initialMintingToKnownAddress(Accounts accounts) {
        logger.traceEntry("params: {}", accounts);
        AccountState accountState = null;

        try {

            //AccountAddress accountAddress = AccountAddress.fromBytes(Util.PUBLIC_KEY_MINTING.getValue());
            AccountAddress accountAddress = AppServiceProvider.getShardingService().getAddressForMinting(accounts.getShard());
            accountState = getOrCreateAccountState(accountAddress, accounts);
            accountState.setBalance(Util.VALUE_MINTING);
            setAccountState(accountAddress, accountState, accounts);
            accounts.getAccountsPersistenceUnit().commit();
            logger.trace("Done initial minting!");
        } catch (Exception ex) {
            logger.catching(ex);
        }
        logger.traceExit();
    }

    public Fun.Tuple2<Block, Transaction> generateGenesisBlock(String initialAddress, BigInteger initialValue, AppState state, AppContext context) {
        logger.traceEntry("params: {} {} {} {}", initialAddress, initialValue, state, context);

        PrivateKey privateKey = context.getPrivateKey();

        Util.check(!(initialAddress == null || initialAddress.isEmpty()), "initialAddress!=null");
        Util.check(!(initialValue.compareTo(BigInteger.ZERO) < 0), "initialValue is less than zero");
        Util.check(privateKey != null, "privateKey!=null");

        if (initialValue.compareTo(Util.VALUE_MINTING) > 0) {
            initialValue = Util.VALUE_MINTING;
        }

        PrivateKey mintingPrivateKey = AppServiceProvider.getShardingService().getPrivateKeyForMinting(state.getShard());
        PublicKey mintingPublicKey = AppServiceProvider.getShardingService().getPublicKeyForMinting(state.getShard());

        logger.trace("Creating mint transaction...");
        Transaction transactionMint = AppServiceProvider.getTransactionService().generateTransaction(mintingPublicKey,
                new PublicKey(Util.hexStringToByteArray(initialAddress)), initialValue, BigInteger.ZERO);
        logger.trace("Signing mint transaction...");
        AppServiceProvider.getTransactionService().signTransaction(transactionMint, mintingPrivateKey.getValue(), mintingPublicKey.getValue());

        logger.trace("Generating genesis block...");
        Block genesisBlock = new Block();
        genesisBlock.setShard(state.getShard());
        genesisBlock.setNonce(BigInteger.ZERO);

        BlockUtil.addTransactionInBlock(genesisBlock, transactionMint);

        logger.trace("Setting timestamp and round...");
        NTPClient ntpClient = state.getNtpClient();
        genesisBlock.setTimestamp(AppServiceProvider.getChronologyService().getSynchronizedTime(ntpClient));
        genesisBlock.setRoundIndex(0);

        HashSet<String> peerId = new HashSet<String>();

        String self = state.getConnection().getPeer().peerID().toString();

        if(!peerId.contains(self)) {
            peerId.add(self);
        }

        genesisBlock.setPeers(peerId.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList()));

        logger.debug("done added {} peers to genesis block", genesisBlock.getPeers());

        logger.trace("Computing state root hash...");
        try {

            AccountsContext accountsContext = new AccountsContext();
            accountsContext.setShard(state.getShard());
            Accounts accountsTemp = new Accounts(accountsContext, new AccountsPersistenceUnit<>(accountsContext.getDatabasePath()));

            ExecutionService executionService = AppServiceProvider.getExecutionService();
            ExecutionReport executionReport = executionService.processTransaction(transactionMint, accountsTemp);
            if (!executionReport.isOk()) {
                return logger.traceExit((Fun.Tuple2<Block, Transaction>) null);
            }
            genesisBlock.setAppStateHash(accountsTemp.getAccountsPersistenceUnit().getRootHash());
            AppBlockManager.instance().signBlock(genesisBlock, privateKey);
            accountsTemp.getAccountsPersistenceUnit().close();
            logger.trace("Genesis block created!");
        } catch (Exception ex) {
            logger.catching(ex);
            logger.traceExit((Fun.Tuple2<Block, Transaction>) null);
        }

        return logger.traceExit(new Fun.Tuple2<>(genesisBlock, transactionMint));
    }
}