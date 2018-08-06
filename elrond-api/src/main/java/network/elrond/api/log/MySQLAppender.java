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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Plugin(name = "MySQLAppender", category = "Core", elementType = "appender", printObject = false)
public class MySQLAppender extends AbstractAppender {

    private Thread threadWork;

    private Object locker = new Object();
    private List<LogEvent> logEventList = new ArrayList<>();

    private static final int BUCKET_SIZE = 500;

    private String nodeName = null;
    private String tableName = null;

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

            if (nodeName == null){
                nodeName = getNodeName();
            }

            if (nodeName == null){
                SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
                nodeName = "ELROND-NODE-" + sdfSource.format(new Date());
            }

            if (tableName == null) {
                System.out.println("MySQL logging system: nodeName = " + nodeName);

                try {
                    String databaseName = connection.getCatalog();

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS cnt\n" +
                            "FROM `information_schema`.`tables` \n" +
                            "WHERE `table_schema` = '" + databaseName +
                            "' AND table_name ='APP_LOGS " + nodeName + "';");

                    if (!resultSet.next()) {
                        throw new Exception("no row");
                    }

                    int counts = resultSet.getInt("cnt");

                    statement.close();

                    if (counts != 0) {
                        tableName = "`APP_LOGS " + nodeName + "`";
                    } else {
                        statement = connection.createStatement();
                        statement.execute("create table `" + databaseName +
                                "`.`APP_LOGS " + nodeName + "`(\n" +
                                "    LOG_ID BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                                "    NODE_NAME varchar(250),\n" +
                                "    ENTRY_DATE timestamp,\n" +
                                "    LOGGER varchar(100),\n" +
                                "    LOG_LEVEL varchar(100),\n" +
                                "    THREAD TEXT,\n" +
                                "    MESSAGE LONGTEXT,\n" +
                                "    EXCEPTION LONGTEXT\n" +
                                ");\n");

                        tableName = "`APP_LOGS " + nodeName + "`";

                        statement.close();
                    }

                } catch (Exception ex) {
                    tableName = "`APP_LOGS`";
                }

                System.out.println("MySQL logging system: table = " + tableName);
            }

            while (listWork.size() > 0) {
                try {
                    PreparedStatement preparedStatement =
                            connection.prepareStatement("INSERT INTO " + tableName + " (NODE_NAME, ENTRY_DATE, LOGGER, LOG_LEVEL, THREAD, MESSAGE, EXCEPTION) VALUES (?, ?, ?, ?, ?, ?, ?)");

                    int limit = 0;

                    for (int i = 0; i < listWork.size(); i++) {
                        LogEvent logEvent = listWork.get(i);



                        if (nodeName != null){
                            preparedStatement.setString(1, nodeName);
                        } else {
                            preparedStatement.setString(1, "Unknown Elrond Node");
                        }
                        preparedStatement.setTimestamp(2, new Timestamp(logEvent.getTimeMillis()));
                        preparedStatement.setString(3, logEvent.getLoggerName());
                        preparedStatement.setString(4, logEvent.getLevel().toString());
                        preparedStatement.setString(5, logEvent.getThreadName());
                        preparedStatement.setString(6, logEvent.getMessage().getFormattedMessage());
                        Throwable throwable = logEvent.getThrown();
                        if (throwable != null){
                            preparedStatement.setString(7, throwable.getMessage() + " > " +
                                    Arrays.toString(throwable.getStackTrace()));
                        } else {
                            preparedStatement.setString(7, "");
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

    public static String getNodeName(){
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            String globalIP = in.readLine();

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            String localPrefIP = socket.getLocalAddress().getHostAddress();

            return globalIP + " / " + localPrefIP; //you get the IP as a String
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }
}
