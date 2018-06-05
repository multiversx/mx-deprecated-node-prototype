package network.elrond.core;

import junit.framework.TestCase;
import network.elrond.data.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class WindowedByteArrayOutputStreamTest {


    @Test(expected = Exception.class)
    public void testInitialSizeNegativeShouldThrowException(){
        WindowedByteArrayOutputStream windowedByteArrayOutputStream = new WindowedByteArrayOutputStream(-1);
    }

    @Test(expected = Exception.class)
    public void testBytesMaxInvalidShouldThrowException(){
        WindowedByteArrayOutputStream windowedByteArrayOutputStream = new WindowedByteArrayOutputStream();
        windowedByteArrayOutputStream.setMaxBytes(0);
    }

    @Test
    public void testAppend1Byte() throws Exception{
        WindowedByteArrayOutputStream windowedByteArrayOutputStream = new WindowedByteArrayOutputStream();
        windowedByteArrayOutputStream.write(65);

        TestCase.assertEquals("A", windowedByteArrayOutputStream.toString("UTF8"));
    }

    @Test
    public void testDoNotOverflow() throws Exception{
        WindowedByteArrayOutputStream windowedByteArrayOutputStream = new WindowedByteArrayOutputStream();

        String strData;

        byte[] buff = new byte[100000];

        for (int i = 0; i < buff.length; i++) {
            buff[i] = 65;
        }

        for (int i = 0; i < 100; i++){
            windowedByteArrayOutputStream.write(buff);

            strData = windowedByteArrayOutputStream.toString("UTF8");

            TestCase.assertTrue(strData.length() <= windowedByteArrayOutputStream.getMaxBytes());
        }
    }

    @Test
    public void testDataTrim() throws Exception{
        WindowedByteArrayOutputStream windowedByteArrayOutputStream = new WindowedByteArrayOutputStream();

        String strData ;

        byte[] buff = new byte[65536];

        //compute buffer in the form CAAAAAAAAAAAAAAA.....
        buff[0] = 67;
        for (int i = 1; i < buff.length; i++) {
            buff[i] = 65;
        }

        for (int i = 0; i < 16; i++){
            windowedByteArrayOutputStream.write(buff);
        }
        strData = windowedByteArrayOutputStream.toString("UTF8");

        //should be CAAAAA......AAACAAAAAAAA.....AAACAAAAA....AAAA
        TestCase.assertTrue(strData.endsWith("A") && strData.startsWith("C"));

        windowedByteArrayOutputStream.write(66);
        strData = windowedByteArrayOutputStream.toString("UTF8");

        //should be AAAAA......AAACAAAAAAAA.....AAACAAAAA....AAAAB
        TestCase.assertTrue(strData.endsWith("B") && !strData.startsWith("C") && strData.startsWith("A"));
    }
}
