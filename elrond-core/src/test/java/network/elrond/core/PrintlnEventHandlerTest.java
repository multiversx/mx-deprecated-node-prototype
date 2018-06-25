package network.elrond.core;

import org.junit.Test;

public class PrintlnEventHandlerTest {

    @Test
    public void testPrinting() {
        EventHandler printlnEventHandler = new PrintlnEventHandler();

        printlnEventHandler.onEvent(null, null);
        printlnEventHandler.onEvent(null, "1");
        printlnEventHandler.onEvent( new Object(), "2");
    }


}
