package com.xy;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.xy.mode.ExacuteCodeRequest;
import com.xy.mode.ExacuteCodeResponse;
import com.xy.mode.ExecuteMessage;
import com.xy.mode.JudgeInfo;
import com.xy.security.DefaultSecurityManager;
import com.xy.security.DenySecurityManager;
import com.xy.utils.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JavaNativeCodeSandbox implements CodeSandbox{

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000l;

    private static final String SECURITY_MANAGER_PATH = "D:\\xuexiDaima\\xyoj-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    private static final List<String> blacklist = Arrays.asList("Files", "exec");

    private static final WordTree WORD_TREE ;

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blacklist);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExacuteCodeRequest exacuteCodeRequest = new ExacuteCodeRequest();
        exacuteCodeRequest.setInputList(Arrays.asList("1 2","1 3"));
// //       String code = ResourceUtil.readStr("textCode/simpleCompute/Main.java", StandardCharsets.UTF_8); //scanner输入
//        String code = ResourceUtil.readStr("textCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("textCode/unsafeCode/ReadFileError.java", StandardCharsets.UTF_8);
        exacuteCodeRequest.setCode(code);
        exacuteCodeRequest.setLanguage("java");
        ExacuteCodeResponse exacuteCodeResponse = javaNativeCodeSandbox.executeCode(exacuteCodeRequest);
        System.out.println(exacuteCodeResponse);

    }
    @Override
    public ExacuteCodeResponse executeCode(ExacuteCodeRequest exacuteCodeRequest) {
        System.setSecurityManager(new DenySecurityManager());

        List<String> inputList = exacuteCodeRequest.getInputList();
        String code = exacuteCodeRequest.getCode();
        String language = exacuteCodeRequest.getLanguage();

        //校验黑名单代码
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if (foundWord != null){
//            System.out.println("代码中包含黑名单"+foundWord.getFoundWord());
//            return null;
//        }

        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir+ File.separator + GLOBAL_CODE_DIR_NAME;
        //判断全局代码目录是否存在,不存在就创建
        if(!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }
        //把用户代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath+File.separator+GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        //2、编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodePath);
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            return  getErrorResponse(e);
        }

        //3、执行代码
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList){
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=s% Main %s",userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME,inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //超时控制
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行",inputArgs); //scanner输入

                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //4、收集整理输出结果
        ExacuteCodeResponse exacuteCodeResponse = new ExacuteCodeResponse();
        List<String > outputList = new ArrayList<>();
        //取用时最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage:executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)){
                //用户提交代码执行中的错误
                exacuteCodeResponse.setMessage(errorMessage);
                exacuteCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null){
                maxTime = Math.max(maxTime,time);
            }
        }
        if (outputList.size() == executeMessageList.size()){
            exacuteCodeResponse.setStatus(1);
        }
        exacuteCodeResponse.setOutputList(outputList);
        //正常运行完成
        exacuteCodeResponse.setStatus(1);
        JudgeInfo judgeInfo = new JudgeInfo();
        for (ExecuteMessage executeMessage : executeMessageList) {
            
        }
//        judgeInfo.setMemory();
        judgeInfo.setTime(maxTime);
        
        exacuteCodeResponse.setJudgeInfo(judgeInfo);

        //5、文件清理
        if(userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del ? "成功" : "失败"));
        }



        return exacuteCodeResponse;
    }
    //6、错误处理，提升程序健壮性
    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private  ExacuteCodeResponse getErrorResponse( Throwable e){
        ExacuteCodeResponse exacuteCodeResponse = new ExacuteCodeResponse();
        exacuteCodeResponse.setOutputList(new ArrayList<>());
        exacuteCodeResponse.setMessage(e.getMessage());
        //代码沙箱的错误
        exacuteCodeResponse.setStatus(2);
        exacuteCodeResponse.setJudgeInfo(new JudgeInfo());
        return exacuteCodeResponse;
    }
}
