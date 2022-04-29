package cn.leftsite.deepltrans.entity;

import java.util.HashMap;

public class R extends HashMap<String, Object> {
    private static final String KEY_DATA = "data";
    private static final String KEY_MSG = "msg";
    private static final String KEY_CODE = "code";

    private static final String CODE_SUCCESS = "success";
    private static final String CODE_ERROR = "error";

    public static R ok() {
        return new R() {
            {
                put(KEY_CODE, CODE_SUCCESS);
            }
        };
    }

    public static R ok(Object data) {
        R ok = ok();
        ok.put(KEY_DATA, data);
        return ok;
    }

    public static R error() {
        return new R() {
            {
                put(KEY_CODE, CODE_ERROR);
            }
        };
    }

    public static R error(String msg) {
        R error = error();
        error.put(KEY_MSG, msg);
        return error;
    }

}
