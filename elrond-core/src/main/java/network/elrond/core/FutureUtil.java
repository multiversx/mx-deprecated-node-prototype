package network.elrond.core;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FutureUtil {

    public interface Task<E> {
        E get() throws Exception;
    }

    public static <E> E get(Task<E> task) throws Exception {
        return get(task, 30L);
    }

    public static <E> E get(Task<E> task, long timeout) throws Exception {
        FutureTask<E> timeoutTask = new FutureTask<>(task::get);
        new Thread(timeoutTask).start();
        return timeoutTask.get(timeout, TimeUnit.SECONDS);

    }


}
