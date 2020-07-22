package club.chillman.rpccommon.exception;

import club.chillman.rpccommon.enumeration.RemoteErrorMessageEnum;

/**
 * RPC 异常类
 * @author NIU
 * @createTime 2020/7/20 18:15
 */
public class RemoteException extends RuntimeException {
    public RemoteException(RemoteErrorMessageEnum errorMessageEnum, String detail) {
        super(errorMessageEnum.getMessage() + ":" + detail);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }
    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(RemoteErrorMessageEnum errorMessageEnum) {
        super(errorMessageEnum.getMessage());
    }
}
