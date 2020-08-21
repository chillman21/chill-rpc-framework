import java.util.*;

/**
 * @author NIU
 * @createTime 2020/7/21 22:04
 */
public class ParallelStreamTest {
    public static void main(String[] args) {

        ArrayList<String> list = new ArrayList<>();
        list.add("aa");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.parallelStream().forEach(num->
                System.out.println(Thread.currentThread().getName()+">>"+num));
    }
}
