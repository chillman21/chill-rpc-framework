package club.chillman.rpccommon.utils;

import javax.annotation.Nullable;

/**
 * @author NIU
 * @createTime 2020/7/20 15:48
 */
public class StringUtils {
    public static boolean isEmpty(@Nullable Object str) {
        return (str == null || "".equals(str));
    }
    public static boolean isEmpty(@Nullable Object str1, @Nullable Object str2) {
        return (str1 == null || "".equals(str1) || str2 == null || "".equals(str2));
    }
}
