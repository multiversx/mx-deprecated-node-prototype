package network.elrond.application;


import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastConnection;

import java.awt.font.TransformAttribute;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppState implements Serializable {

    private boolean stillRunning = true;

    private P2PBroadcastConnection connection;
    private Map<String, P2PBroadcastChanel> channels = new HashMap<>();

    //private Hashtable<String, Transaction> transactionPool = new Hashtable<>();
    //private ReentrantLock lockTxPool = new ReentrantLock();

    //main structure to hold transaction pool
    private ConcurrentHashMap <String, Transaction> transactionPool = new ConcurrentHashMap<>();

    //array list of hashes of transactions to pe fetched from DHT
    private List<String> listOfTxToProcess = new ArrayList<>();
    private ReentrantLock lockListOfTxToProcess = new ReentrantLock();

    public P2PBroadcastChanel getChanel(String name) {
        return channels.get(name);
    }

    public void addChanel(String name, P2PBroadcastChanel chanel) {
        this.channels.put(name, chanel);
    }

    public P2PBroadcastConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PBroadcastConnection connection) {
        this.connection = connection;
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public void setStillRunning(boolean stillRunning) {
        this.stillRunning = stillRunning;
    }

    public Boolean containsTxHashInTxPool(String strHash) {
            return(transactionPool.contains(strHash));
    }

    public void addTxToTxPool(String strHash, Transaction tx){
        transactionPool.put(strHash, tx);
    }

    public int getTxPoolSize(){
        return (transactionPool.size());
    }

    public void pushToTxToProcess(String strData){
        lockListOfTxToProcess.lock();
        try {
            listOfTxToProcess.add(strData);
        } finally {
            lockListOfTxToProcess.unlock();
        }
    }

    public String popFromTxToProcess(){
        lockListOfTxToProcess.lock();
        try {
            if (listOfTxToProcess.size() > 0)
            {
                String strData = listOfTxToProcess.get(0);
                listOfTxToProcess.remove(0);
                return (strData);
            } else {
                return (null);
            }
        } finally {
            lockListOfTxToProcess.unlock();
        }
    }
}