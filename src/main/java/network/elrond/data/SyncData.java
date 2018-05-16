package network.elrond.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The SyncData class is used to control access to the data it holds
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-16
 * @param <T> Type of object used in data structures
 */
public class SyncData <T>{
    //main structure to hold data as pool
    private ConcurrentHashMap<String, T> objectPool;

    //array list of hashes of objects to pe fetched from DHT
    private List<String> listOfHashesToProcess;
    private ReentrantLock lockListOfHashesToProcess;

    public SyncData() {
        objectPool = new ConcurrentHashMap<>();
        listOfHashesToProcess = new ArrayList<>();
        lockListOfHashesToProcess = new ReentrantLock();
    }

    /**
     * Method used to check whether a hash is present or not in main structure data
     * @param strHash to be checked
     * @return True or False, accordingly
     */
    public Boolean containsHashInObjectPool(String strHash) {
        return(objectPool.contains(strHash));
    }

    /**
     * Method used to add data
     * @param strHash hash of the object (eg. ID)
     * @param obj object to be added
     */
    public void addObjToObjectPool(String strHash, T obj){
        objectPool.put(strHash, obj);
    }

    /**
     * Gets the object pool size
     * @return the size as int
     */
    public int getObjectPoolSize(){
        return (objectPool.size());
    }

    /**
     * Push hash to the list of need to-be-processed-hashes-list
     * @param strData to be added
     */
    public void pushToHashesToProcess(String strData){
        lockListOfHashesToProcess.lock();
        try {
            listOfHashesToProcess.add(strData);
        } finally {
            lockListOfHashesToProcess.unlock();
        }
    }

    /**
     * Return the first hash from the to-be-processed-hashes-list
     * @return the first hash or null if list is empty
     */
    public String popFromHashesToProcess(){
        lockListOfHashesToProcess.lock();
        try {
            if (listOfHashesToProcess.size() > 0)
            {
                String strData = listOfHashesToProcess.get(0);
                listOfHashesToProcess.remove(0);
                return (strData);
            } else {
                return (null);
            }
        } finally {
            lockListOfHashesToProcess.unlock();
        }
    }

}
