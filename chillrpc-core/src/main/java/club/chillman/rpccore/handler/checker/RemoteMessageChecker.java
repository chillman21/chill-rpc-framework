package club.chillman.rpccore.handler.checker;

import club.chillman.rpccommon.enumeration.RemoteErrorMessageEnum;
import club.chillman.rpccommon.enumeration.RemoteResponseCode;
import club.chillman.rpccommon.exception.RemoteException;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 校验 RemoteRequest 和 RemoteRequest
 * @author NIU
 * @createTime 2020/7/21 17:00
 */
@Slf4j
public final class RemoteMessageChecker {
    private static final String INTERFACE_NAME = "interfaceName";

    private RemoteMessageChecker() {
    }

    public static void check(RemoteRequest remoteRequest, RemoteResponse remoteResponse) {
        if (remoteResponse == null) {
            throw new RemoteException(RemoteErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + remoteRequest.getInterfaceName());
        }

        if (!remoteResponse.getResponseId().equals(remoteRequest.getRequestId())) {
            throw new RemoteException(RemoteErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + remoteRequest.getInterfaceName());
        }

        if (remoteResponse.getCode() == null || !remoteResponse.getCode().equals(RemoteResponseCode.SUCCESS.getCode())) {
            throw new RemoteException(RemoteErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + remoteRequest.getInterfaceName());
        }
    }
}
