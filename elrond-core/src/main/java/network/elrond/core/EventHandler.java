package network.elrond.core;

import network.elrond.Application;

public interface EventHandler<D, Q> {
    void onEvent(Application application, Object sender, D data, Q queue);
}
