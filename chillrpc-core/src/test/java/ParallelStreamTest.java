import java.util.Arrays;
import java.util.List;

/**
 * @author NIU
 * @createTime 2020/7/21 22:04
 */
public class ParallelStreamTest {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.parallelStream().forEach(num->
                System.out.println(Thread.currentThread().getName()+">>"+num));
    }
}
