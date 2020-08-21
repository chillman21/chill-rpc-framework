package club.chillman.serviceapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author NIU
 * @createTime 2020年07月20日 02:21:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class Test implements Serializable {
    private static final long serialVersionUID = -3325838916375597109L;
    private String str;
    private int num;
}
