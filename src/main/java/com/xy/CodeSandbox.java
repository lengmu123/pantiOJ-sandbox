package com.xy;


import com.xy.mode.ExecuteCodeRequest;
import com.xy.mode.ExecuteCodeResponse;

import java.io.IOException;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {
    /**
     * 执行代码
     * @param request
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest request) throws IOException;
}
