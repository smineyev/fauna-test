package fauna.test;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Utils {

    public interface UncheckedRunnable {
        void run() throws Exception;
    }

    public static void unchecked(UncheckedRunnable r)  {
        try {
            r.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static long timeit(UncheckedRunnable code) throws Exception {
        long start = System.currentTimeMillis();
        code.run();
        return System.currentTimeMillis() - start;
    }

    public static void timeit(String codeDesc, UncheckedRunnable code) throws Exception {
        long start = System.currentTimeMillis();
        code.run();
        System.out.println(codeDesc+ " executed in " + (System.currentTimeMillis() - start) +"ms");
    }

}
