package network.elrond.api;

import ch.qos.logback.classic.Level;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LoggerUtilsTest {
    @Test
    public void gettingPathShouldNotThrowExceptionAndNotBeNull() throws Exception{

        String path = new File(".").getCanonicalPath();
        System.out.println(path);

        File filePattern = new File(path, "app.%i.log");

        System.out.println(filePattern);

        TestCase.assertNotNull(path);
    }

    @Test
    public void creatingDirectoryForLoggerShouldNotFail() throws Exception{
        LoggerUtils loggerUtils = new LoggerUtils();

        String logPath = loggerUtils.createLogFolderAndReturnPath();

        System.out.println(logPath);

        TestCase.assertNotNull(logPath);
    }

    @Test
    public void testRollingFileAppender() throws Exception{
        LoggerUtils loggerUtils = new LoggerUtils();

        //delete logs contents
        String path = loggerUtils.createLogFolderAndReturnPath();
        FileUtils.deleteDirectory(new File(path));
        path = loggerUtils.createLogFolderAndReturnPath();

        loggerUtils.AddLogAppenderRollingFile(Level.ALL);

        Logger logger = LoggerFactory.getLogger(this.getClass().getName());

        for (int i = 0; i < 150000; i++) {
            logger.info("Test write on disk");
        }

        //check for files
        TestCase.assertTrue(FileUtils.directoryContains(new File(path), new File(path,"app.log")));
        TestCase.assertTrue(FileUtils.directoryContains(new File(path), new File(path,"app.1.log")));
        TestCase.assertTrue(FileUtils.directoryContains(new File(path), new File(path,"app.2.log")));
    }


}
