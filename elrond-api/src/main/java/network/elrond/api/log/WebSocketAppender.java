package network.elrond.api.log;


import network.elrond.api.manager.ElrondWebSocketManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;


@Plugin(name = "WebSocketAppender", category = "Core", elementType = "appender", printObject = false)
public class WebSocketAppender extends AbstractAppender {


    protected WebSocketAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    protected WebSocketAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @PluginFactory
    public static WebSocketAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for WebSocketAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new WebSocketAppender(name, filter, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        ElrondWebSocketManager elrondWebSocketManager = WebSocketAppenderAdapter.instance().getElrondWebSocketManager();
        if (elrondWebSocketManager == null) {
            return;
        }
        elrondWebSocketManager.announce("/topic/public", event + "");

    }

}
