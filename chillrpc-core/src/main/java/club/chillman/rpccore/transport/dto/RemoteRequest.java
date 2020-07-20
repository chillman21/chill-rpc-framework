package club.chillman.rpccore.transport.dto;

import club.chillman.rpccommon.enumeration.RemoteMessageTypeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * RPC请求实体类
 * @author NIU
 * @createTime 2020/7/20 2:46
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RemoteRequest implements Serializable {
    private static final long serialVersionUID = 270512241950251207L;
    // RPC请求的ID
    private String requestId;
    // 所请求的接口名称
    private String interfaceName;
    // 所请求的方法名称
    private String methodName;
    // 所请求的输入参数数组
    private Object[] parameters;
    // 所请求的输入参数的Class类型数组
    private Class<?>[] paramTypes;
    // 是否需要心跳连接
    private RemoteMessageTypeEnum remoteMessageTypeEnum;
}
