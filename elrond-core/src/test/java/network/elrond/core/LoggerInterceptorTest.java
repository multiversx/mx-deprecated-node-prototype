package network.elrond.core;

import ch.qos.logback.classic.selector.servlet.LoggerContextFilter;
import junit.framework.TestCase;
import network.elrond.p2p.PingResponse;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerInterceptorTest {

    @Test
    public void InterceptLogger() throws Exception{
        //Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger logger = LoggerFactory.getLogger(LoggerInterceptorTest.class);

        logger.info("This is a test for ByteArrayOutputStreamAppender");




        //not null
        //TestCase.assertNotNull(ByteArrayOutputStreamAppender.getMainOutputStream());

        //contains the text
        //String data = ByteArrayOutputStreamAppender.getMainOutputStream().toString("UTF8");
        //TestCase.assertTrue(data.contains("This is a test for ByteArrayOutputStreamAppender"));




        //clear
        //ByteArrayOutputStreamAppender.clearMainOutputStream();
        //data = ByteArrayOutputStreamAppender.getMainOutputStream().toString("UTF8");
        //TestCase.assertEquals(data, "");

    }
}
