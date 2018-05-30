package network.elrond.api;

import network.elrond.Application;
import network.elrond.application.AppContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class ElrondApplicationRunner implements DisposableBean {

    private Application application;

    public void start(AppContext context) {

        application = new Application(context);
        try {
            application.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        application.stop();
    }

    public Application getApplication() {
        return application;
    }
}