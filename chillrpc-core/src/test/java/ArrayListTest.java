import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author NIU
 * @createTime 2020/7/25 0:41
 */
public class ArrayListTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        Man man = new Man();
//        man.setName("niu");
//        col(man);
//        System.out.println(man);
        //创建FutureTask的对象
        FutureTask<String> task = new FutureTask<String>(new ThreadDemo3());
        Thread thread3 = new Thread(task);
        thread3.setName("线程三");
        //开启线程
        thread3.start();
        //获取call()方法的返回值，即线程运行结束后的返回值
        String result = task.get();
        System.out.println(result);


    }
    static class ThreadDemo3 implements Callable<String> {

        @Override
        public String call() throws Exception {
            System.out.println(Thread.currentThread().getName()+":"+"输出的结果");
            return Thread.currentThread().getName()+":"+"返回的结果";
        }
    }
    public static void col(Man man) {
        man.setName("azhe");
        man = Man.builder().name("aa").build();
    }


}
