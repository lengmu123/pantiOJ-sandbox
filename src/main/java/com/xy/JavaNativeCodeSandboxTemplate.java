package com.xy;


import com.xy.mode.ExecuteCodeRequest;
import com.xy.mode.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public  class JavaNativeCodeSandboxTemplate  extends JavaCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest exacuteCodeRequest) throws IOException {
        return super.executeCode(exacuteCodeRequest);
    }
}