package com.linhonghong.demo.dilutions;

import com.linhonghong.dilutions.annotations.ExtraParam;
import com.linhonghong.dilutions.annotations.ProtocolPath;

/**
 * Created by Linhh on 17/3/7.
 */

public interface DebugService {

    @ProtocolPath("/test")
    void renderPage(@ExtraParam("test") String test, @ExtraParam("id") Object obj);
}
