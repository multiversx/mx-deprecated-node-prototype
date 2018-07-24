package network.elrond.api.log;

import network.elrond.core.ThreadUtil;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Plugin(name = "MySQLAppender", category = "Core", elementType = "appender", printObject = false)
public class MySQLAppender extends AbstractAppender {

    private Thread threadWork;

    private Object locker = new Object();
    private List<LogEvent> logEventList = new ArrayList<>();

    private static final int BUCKET_SIZE = 1000;

    private String nodeName = null;

    protected MySQLAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);

        threadWork = new Thread(() -> {
            doContinousAppend();
        });
        threadWork.start();

    }

    protected MySQLAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, String connection) {
        super(name, filter, layout, ignoreExceptions);

        this.connection = connection;

        threadWork = new Thread(() -> {
            doContinousAppend();
        });
        threadWork.start();
    }

    private boolean isAppenderStopped(){
        return (this.getState() == State.STOPPING) || (this.getState() == State.STOPPED);
    }

    private void doContinousAppend(){
        while (true){
            ThreadUtil.sleep(1);

            if (isAppenderStopped()){
                return;
            }

            List<LogEvent> listWork = new ArrayList<>();

            synchronized (locker){
                listWork = new ArrayList<>(logEventList);
                logEventList.clear();
            }

            if (listWork.size() == 0){
                continue;
            }

            if (isAppenderStopped()){
                return;
            }

            Connection connection = getConnection();

            if ((this.getState() == State.STOPPING) || (this.getState() == State.STOPPED)){
                return;
            }

            if (connection == null){
                continue;
            }

            while (listWork.size() > 0) {
                try {
                    PreparedStatement preparedStatement =
                            connection.prepareStatement("INSERT INTO APP_LOGS (NODE_NAME, ENTRY_DATE, LOGGER, LOG_LEVEL, MESSAGE, EXCEPTION) VALUES (?, ?, ?, ?, ?, ?)");

                    int limit = 0;

                    for (int i = 0; i < listWork.size(); i++) {
                        LogEvent logEvent = listWork.get(i);

                        if (nodeName == null){
                            nodeName = getNodeName();
                        }

                        if (nodeName != null){
                            preparedStatement.setString(1, nodeName);
                        } else {
                            preparedStatement.setString(1, "Unknown Elrond Node");
                        }
                        preparedStatement.setTimestamp(2, new Timestamp(logEvent.getTimeMillis()));
                        preparedStatement.setString(3, logEvent.getLoggerName());
                        preparedStatement.setString(4, logEvent.getLevel().toString());
                        preparedStatement.setString(5, logEvent.getMessage().getFormattedMessage());
                        Throwable throwable = logEvent.getThrown();
                        if (throwable != null){
                            preparedStatement.setString(6, throwable.getMessage());
                        } else {
                            preparedStatement.setString(6, "");
                        }
                        preparedStatement.addBatch();

                        listWork.remove(i);
                        i--;
                        limit++;

                        if (limit > BUCKET_SIZE){
                            break;
                        }
                    }

                    preparedStatement.executeBatch();
                    preparedStatement.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    listWork.clear();
                }
            }
        }
    }

    @PluginFactory
    public static MySQLAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("connection") String connection) {
        if (name == null) {
            LOGGER.error("No name provided for MySQLAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new MySQLAppender(name, filter, layout, true, connection);
    }

    @PluginBuilderAttribute
    @Required
    private String connection;


    private Connection getConnection(){
        //"jdbc:mysql://46.101.242.65:3306/LOGS?useSSL=false&useServerPrepStmts=false&rewriteBatchedStatements=true&user=loguser&password=elrondCaps67"
        try {
            Connection conn = DriverManager.getConnection(connection);
            //connection.setAutoCommit(false);
            return conn;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return (null);
    }

    @Override
    public void append(LogEvent event) {
        synchronized (logEventList){
            logEventList.add(event);
        }
    }

    private String getNodeName(){
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            String globalIP = in.readLine();

            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String localPrefIP = socket.getLocalAddress().getHostAddress();

            return globalIP + " / " + localPrefIP; //you get the IP as a String
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }
}
