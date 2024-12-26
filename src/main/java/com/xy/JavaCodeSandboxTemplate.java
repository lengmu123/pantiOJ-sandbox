package com.xy;

import com.xy.mode.ExecuteCodeRequest;
import com.xy.mode.ExecuteCodeResponse;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.xy.mode.ExecuteMessage;
import com.xy.mode.JudgeInfo;
import com.xy.security.DenySecurityManager;
import com.xy.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox{

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000l;

    private static final String SECURITY_MANAGER_PATH = "D:\\xuexiDaima\\xyoj-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "DefaultSecurityManager";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest exacuteCodeRequest) {
//        System.setSecurityManager(new DenySecurityManager());

        List<String> inputList = exacuteCodeRequest.getInputList();
        String code = exacuteCodeRequest.getCode();
        String language = exacuteCodeRequest.getLanguage();

        //校验黑名单代码
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if (foundWord != null){
//            System.out.println("代码中包含黑名单"+foundWord.getFoundWord());
//            return null;
//        }
        //1、把用户代码保存为文件
        File userCodeFile = saveCodeFile(code);

        //2、编译代码，得到class文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);

        //3、执行代码
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

        //4、收集整理输出结果
        ExecuteCodeResponse outPutResponse = getOutPutResponse(executeMessageList);

        //5、文件清理
        boolean del = deleteFile(userCodeFile);
        if (!del){
            log.error("删除失败，userCodeFilePath={}",userCodeFile.getAbsolutePath());
        }


        return outPutResponse;
    }

    /**
     * 1、把用户代码保存为文件
     * @return
     */
    public File saveCodeFile(String code){
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
        return userCodeFile;

    }

    /**
     * 2、编译代码，得到class文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
//            System.out.println(executeMessage);
            if (executeMessage.getExitValue() != 0){
                throw new RuntimeException("编译错误！");
            }
            return executeMessage;
        } catch (Exception e) {
//            return  getErrorResponse(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * //3、执行代码,获得执行结果列表
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile,List<String> inputList){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList){
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME,inputArgs);

            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //超时控制
//                new Thread(()->{
//                    try {
//                        Thread.sleep(TIME_OUT);
//                        System.out.println("超时了");
//                        runProcess.destroy();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行",inputArgs); //scanner输入

                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("程序执行异常",e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4、获取输出结果
     * @param executeMessageList
     */
    public ExecuteCodeResponse getOutPutResponse(List<ExecuteMessage> executeMessageList){
        ExecuteCodeResponse exacuteCodeResponse = new ExecuteCodeResponse();
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
        //正常运行完成
        if (outputList.size() == executeMessageList.size()){
            exacuteCodeResponse.setStatus(1);
        }
        exacuteCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        //        judgeInfo.setMemory();

        exacuteCodeResponse.setJudgeInfo(judgeInfo);
        return exacuteCodeResponse;
    }

    /**
     * 5、删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        if(userCodeFile.getParentFile() != null){
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    //6、错误处理，提升程序健壮性
    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse exacuteCodeResponse = new ExecuteCodeResponse();
        exacuteCodeResponse.setOutputList(new ArrayList<>());
        exacuteCodeResponse.setMessage(e.getMessage());
        //代码沙箱的错误
        exacuteCodeResponse.setStatus(2);
        exacuteCodeResponse.setJudgeInfo(new JudgeInfo());
        return exacuteCodeResponse;
    }

}
