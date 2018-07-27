package network.elrond.p2p;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;

public class DirectBaseFutureListener<F> implements BaseFutureListener<net.tomp2p.futures.FutureDirect> {
    private Object object = null;

    @Override
    public void operationComplete(FutureDirect futureDirect) throws Exception {
        if (futureDirect.isCompleted() && futureDirect.isSuccess()){
            object = futureDirect.object();
        }
    }

    @Override
    public void exceptionCaught(Throwable t) throws Exception {

    }

    public Object getObject(){
        return(object);
    }
}
