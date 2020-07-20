import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author NIU
 * @createTime 2020/7/20 22:33
 */
public class CompletableFutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(()->{
            System.out.println("sb");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete("sb");
        }).start();
        System.out.println(future.get());

    }
}
