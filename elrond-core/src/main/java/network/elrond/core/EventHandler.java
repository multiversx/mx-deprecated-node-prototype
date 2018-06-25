package network.elrond.core;

public interface EventHandler<T> {
    void onEvent(Object sender, T data);
}
