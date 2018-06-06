package network.elrond.core;

import ch.qos.logback.core.Appender;
import junit.framework.TestCase;
import network.elrond.service.AppServiceProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class LoggerInterceptorTest {

    @Test(expected = Exception.class)
    public void testNotExistentAppenderShouldThrowException() throws Exception{
        OutputStream loggerStream = AppServiceProvider.getLoggerService().getLoggerStream("NON EXISTENT");
    }

    @Test(expected = Exception.class)
    public void testNotExistentAppenderShouldThrowException2() throws Exception{
        Appender appender = AppServiceProvider.getLoggerService().getLoggerAppender("NON EXISTENT");
    }

    @Ignore
    @Test
    public void InterceptLogger() throws Exception{
        //Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger logger = LoggerFactory.getLogger(LoggerInterceptorTest.class);

        logger.info("This is a test for ByteArrayOutputStreamAppender");

        OutputStream loggerStream = AppServiceProvider.getLoggerService().getLoggerStream("BYTEARRAY");

        //not null
        TestCase.assertNotNull(loggerStream);

        //contains the text
        String data = ((WindowedByteArrayOutputStream)loggerStream).toString("UTF8");
        TestCase.assertTrue(data.contains("This is a test for ByteArrayOutputStreamAppender"));

        //clear
        Appender appender = AppServiceProvider.getLoggerService().getLoggerAppender("BYTEARRAY");
        ((ByteArrayOutputStreamAppender)appender).clearMainOutputStream();
        data = ((WindowedByteArrayOutputStream)loggerStream).toString("UTF8");
        TestCase.assertEquals(data, "");

    }
}
