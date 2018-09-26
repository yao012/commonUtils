import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtil {

    public interface OnExecute<T> {
        void run(T t) throws Exception;
    }

    public static <T> void exeucteBat(Collection<T> inputs, OnExecute<T> exec) {
        exeucteBat(4, inputs, exec);
    }

    public static <T> void exeucteBat(final int maxThread, Collection<T> inputs, OnExecute<T> exec) {
        int index = 0;
        ExecutorService executor = Executors.newFixedThreadPool(maxThread);
        try {
            Future<?>[] futures = new Future<?>[maxThread];
            for (final T t : inputs) {
                futures[index++] = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            exec.run(t);
                        } catch (Exception ex) {

                        }
                    }
                });
                if (index == maxThread) {
                    for (int i = 0; i < index; i++) {
                        try {
                            futures[i].get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        futures[i] = null;
                    }
                    index = 0;
                }
            }
            if (index > 0) {
                for (int i = 0; i < index; i++) {
                    try {
                        futures[i].get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    futures[i] = null;
                }
            }
        } finally {
            executor.shutdown();
        }
    }

}
