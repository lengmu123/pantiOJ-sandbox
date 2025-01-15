package com.xy.controller;

import com.xy.JavaDockerCodeSandbox;
import com.xy.JavaDockerCodeSandboxTemplate;
import com.xy.JavaNativeCodeSandbox;
import com.xy.JavaNativeCodeSandboxTemplate;
import com.xy.mode.ExecuteCodeRequest;
import com.xy.mode.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController()
public class MainController {
    @Resource
    private JavaNativeCodeSandboxTemplate javaNativeCodeSandboxTemplate;

    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    //定义鉴权请求头和密钥
    private final static String AUTH_REQUEST_HEADER = "auth";
    private final static String AUTH_REQUEST_SECRET = "secretKey";

    @GetMapping("/health")
    public String healthCheck(){
        return "okk";
    }

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode/yuan")
    ExecuteCodeResponse executeCodeYuan(@RequestBody  ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!authHeader.equals(AUTH_REQUEST_SECRET)){
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }

        return javaNativeCodeSandboxTemplate.executeCode(executeCodeRequest);
    }

    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody  ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!authHeader.equals(AUTH_REQUEST_SECRET)){
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }

        return javaDockerCodeSandbox.executeCode(executeCodeRequest);
    }
}
