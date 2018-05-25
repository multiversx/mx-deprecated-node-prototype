package network.elrond.processor;

import network.elrond.Application;

import java.io.IOException;

public interface AppProcessor {

    void process(Application application) throws IOException;

}
