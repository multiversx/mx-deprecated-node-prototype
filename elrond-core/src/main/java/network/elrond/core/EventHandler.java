package network.elrond.core;

import network.elrond.Application;

public interface EventHandler<T> {
    void onEvent(Application application, Object sender, T data);
}
