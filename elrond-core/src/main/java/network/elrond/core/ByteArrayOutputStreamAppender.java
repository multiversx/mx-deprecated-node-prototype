package network.elrond.core;

import ch.qos.logback.core.OutputStreamAppender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayOutputStreamAppender<E> extends OutputStreamAppender<E> {
    private WindowedByteArrayOutputStream mainDataStream = new WindowedByteArrayOutputStream();

    public ByteArrayOutputStreamAppender(){
        mainDataStream.setMaxBytes(1048576 * 5); //5MB data storage
    }

    @Override
    public void start() {
        setOutputStream(mainDataStream);
        super.start();
    }

    public void clearMainOutputStream(){
        synchronized (lock){
            mainDataStream.reset();
        }
    }

    public WindowedByteArrayOutputStream getMainOutputStream(){
        return (mainDataStream);
    }
}
