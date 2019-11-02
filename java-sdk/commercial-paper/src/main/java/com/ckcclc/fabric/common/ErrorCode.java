package com.ckcclc.fabric.common;

public enum ErrorCode {

    SUCCESS(0, "success"),
    REQUEST_PARAMETER_ERROR(10000, "Request parameter error"),     // 接口参数错误,
    INTERNAL_SERVER_ERROR(20000, "Internal server error"),      // 系统内部错误

    CHAINCOED_SERVICE_ERROR(30000, "Chaincode service error"),      // chaincode业务错误
    CHAINCOED_INVOKE_ERROR(30001, "Chaincode invoke error"),      // chaincode调用错误

    ;

    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
