package network.elrond.plaintests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlainTest01 {

    public static void main(String[] args)
    {
        String hostName = "127.0.0.1";
        int portNumber = 4096;

        Socket sk;

        try {
            sk = new Socket(hostName, portNumber);


        }
        catch (Exception ex) {
            LoggerFactory.getLogger("elrond.PlainTest01").info(ex.getMessage());
            LoggerFactory.getLogger("PlainTest01").error(ex.getMessage() + " billing");
            LoggerFactory.getLogger("PlainTest01").warn(ex.getMessage());
        }
    }


}
