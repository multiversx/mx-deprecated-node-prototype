package network.elrond.api;

import junit.framework.TestCase;
import network.elrond.core.ResponseObject;
import network.elrond.core.ThreadUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CommandLinesInterpretorTest {

    @Test
    public void testDisplayHelp(){
        CommandLinesInterpretor.displayHelp();
    }

    @Test
    public void testInterpretWithNull(){
        ResponseObject result = CommandLinesInterpretor.interpretCommandLines(null);

        TestCase.assertTrue(result.isSuccess());
        TestCase.assertNull(result.getPayload());
    }

    @Test
    public void testInterpretWithEmpty(){
        ResponseObject result = CommandLinesInterpretor.interpretCommandLines(new String[0]);

        TestCase.assertTrue(result.isSuccess());
        TestCase.assertNull(result.getPayload());
    }

    @Test
    public void testInterpretWithHelp(){
        ResponseObject result = CommandLinesInterpretor.interpretCommandLines(new String[]{"--h"});

        TestCase.assertFalse(result.isSuccess());
        TestCase.assertNull(result.getPayload());
    }

    @Test
    public void testInterpretWithConfigNOK(){
        ResponseObject result = CommandLinesInterpretor.interpretCommandLines(new String[]{"--config=aaa"});

        TestCase.assertFalse(result.isSuccess());
        TestCase.assertNull(result.getPayload());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInterpretWithConfigOK() throws IOException {
        FileWriter fileWriter = new FileWriter("test.config", false);

        fileWriter.append("node_name=elrond-node-1\n" +
                "port=4001\n" +
                "master_peer_port=4000\n" +
                "peer_ip=127.0.0.1\n" +
                "node_private_key=00e073884464d8d887568d6e4e66344db01334436c817bce17653eaf3e428b6ef5\n" +
                "startup_type=START_FROM_SCRATCH\n" +
                "blockchain_path=elrond-node-1\n" +
                "blockchain_restore_path=elrond-node-1");
        fileWriter.close();

        ResponseObject result = CommandLinesInterpretor.interpretCommandLines(new String[]{"--config=test.config"});

        ThreadUtil.sleep(1000);
        FileUtils.forceDelete(new File("test.config"));

        TestCase.assertTrue(result.isSuccess());
        TestCase.assertNotNull(result.getPayload());

        Map<String, Object> data = (Map<String, Object>)result.getPayload();

        for (String key : data.keySet()){
            System.out.println(key + ": " + data.get(key).toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInterpretWithConfigOK2() throws IOException {
        FileWriter fileWriter = new FileWriter("test.config", false);

        fileWriter.append("node_name=AUTO\n" +
                "port=4001\n" +
                "master_peer_port=4000\n" +
                "peer_ip=127.0.0.1\n" +
                "node_private_key=AUTO\n" +
                "startup_type=START_FROM_SCRATCH\n" +
                "blockchain_path=AUTO\n" +
                "blockchain_restore_path=AUTO");
        fileWriter.close();

        ResponseObject result = CommandLinesInterpretor.interpretCommandLines(new String[]{"--config=test.config"});

        ThreadUtil.sleep(1000);
        FileUtils.forceDelete(new File("test.config"));

        TestCase.assertTrue(result.isSuccess());
        TestCase.assertNotNull(result.getPayload());

        Map<String, Object> data = (Map<String, Object>)result.getPayload();

        for (String key : data.keySet()){
            System.out.println(key + ": " + data.get(key).toString());
        }
    }



}
