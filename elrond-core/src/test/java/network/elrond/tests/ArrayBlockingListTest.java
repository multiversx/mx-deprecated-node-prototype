package network.elrond.tests;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingListTest {
    @Test
    public void testArrayBlockingQueue(){
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(50000, true);

        Thread threadProducer = new Thread(()->{
            int counter = 0;

            while(true){
                //arrayBlockingQueue.add(String.valueOf(counter));



            }



        });





    }

}
