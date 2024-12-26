package com.xy;


import com.xy.mode.ExecuteCodeRequest;
import com.xy.mode.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public abstract class JavaNativeCodeSandboxTemplate  extends JavaCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest exacuteCodeRequest) {
        return super.executeCode(exacuteCodeRequest);
    }
}