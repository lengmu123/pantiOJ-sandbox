package com.xy;


import com.xy.mode.ExacuteCodeRequest;
import com.xy.mode.ExacuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {
    /**
     * 执行代码
     * @param request
     * @return
     */
    ExacuteCodeResponse executeCode(ExacuteCodeRequest request);
}
