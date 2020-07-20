package club.chillman.rpccommon.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * RPC响应包状态码枚举类
 * @author NIU
 * @createTime 2020/7/20 12:57
 */
@AllArgsConstructor
@Getter
@ToString
public enum RemoteResponseCode {

    SUCCESS(200, "调用方法成功"),
    FAIL(501, "调用方法失败"),
    METHOD_NOT_FOUND(502, "未找到指定方法"),
    CLASS_NOT_FOUND(503, "未找到指定类");
    // 状态码
    private final int code;
    // 正确/错误信息
    private final String message;

}
