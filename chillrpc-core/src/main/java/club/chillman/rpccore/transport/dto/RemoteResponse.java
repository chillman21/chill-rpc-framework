package club.chillman.rpccore.transport.dto;

import club.chillman.rpccommon.enumeration.RemoteResponseCode;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.*;

import java.io.Serializable;

/**
 * @author NIU
 * @createTime 2020/7/20 2:46
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RemoteResponse<T> implements Serializable {

    private static final long serialVersionUID = -1870038525818498001L;
    // 响应ID
    private String responseId;
    // 响应码
    private Integer code;
    // 响应消息
    private String message;
    // 响应数据
    private T data;

    public static <T> RemoteResponse<T> ok(T data, String responseId){
        RemoteResponse<T> response = RemoteResponse.<T>builder().build();
        response.setCode(RemoteResponseCode.SUCCESS.getCode());
        response.setMessage(RemoteResponseCode.SUCCESS.getMessage());
        response.setResponseId(responseId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RemoteResponse<T> error(RemoteResponseCode responseCode){
        return RemoteResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .build();
    }
}
