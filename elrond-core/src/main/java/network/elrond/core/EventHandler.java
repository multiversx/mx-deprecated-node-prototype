package network.elrond.core;

import network.elrond.Application;

//public interface EventHandler<D, Q> {
public interface EventHandler<D> {
    //void onEvent(Application application, Object sender, D data, Q queue);
    void onEvent(Application application, Object sender, D data);
}
