package com.karlexyan.yojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeRequest;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeResponse;
import com.karlexyan.yojcodesandbox.model.ExecuteMessage;
import com.karlexyan.yojcodesandbox.model.JudgeInfo;
import com.karlexyan.yojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 代码沙箱模版方法的视线
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000L;

    private static final Integer SUCCEED = 2;
    private static final Integer FAILED = 3;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        ExecuteCodeResponse outputResponse = new ExecuteCodeResponse();


        // 1.把用户代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 2.编译代码,得到class文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        if (compileFileExecuteMessage != null && compileFileExecuteMessage.getExitValue() != null) {
            // 编译错误，直接返回
            if(!compileFileExecuteMessage.getExitValue().equals(0)){
                List<String> outputList = new ArrayList<>();
                outputList.add(compileFileExecuteMessage.getMessage());
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setTime(compileFileExecuteMessage.getTime());

                outputResponse.setOutputList(outputList);
                outputResponse.setMessage(compileFileExecuteMessage.getErrorMessage());
                outputResponse.setStatus(FAILED);//返回失败
                outputResponse.setJudgeInfo(judgeInfo);
                return outputResponse;
            }
        }

        // 3.执行代码,得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

        // 4.收集整理输出结果
        outputResponse = getOutputResponse(executeMessageList);

        // 5.文件清理
        boolean flag = deleteFile(userCodeFile);
        if (!flag) {
            log.error("delete failed, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }

        return outputResponse;
    }

    /**
     * 1.把用户代码保存为文件
     *
     * @param code 用户提交代码
     * @return 写入文件夹，并返回生成的文件
     */
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME; // File.separator 可以根据系统分配分隔符 Linux Mac Windows都不一样
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            // 不存在，则创建
            FileUtil.mkdir(globalCodePathName);
        }
        // 将用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();  // 代码存放文件夹路径
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;  // 代码文件路径

        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8); // 使用UTF-8编码写入;
    }

    /**
     * 2.编译代码,得到class文件
     *
     * @param userCodeFile 要编译的文件
     * @return 编译信息
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath()); // 编译命令
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            return ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");  // 调用工具类，编译并获取编译信息
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3.执行代码,得到输出结果
     *
     * @param userCodeFile 要执行的文件
     * @param inputList    输入用例
     * @return 执行信息
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            System.out.println(runCmd);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "执行");// 执行并获取执行信息
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("执行错误", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4.收集整理输出结果
     *
     * @param executeMessageList 执行信息
     * @return 响应对象
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>(); // 控制台输出List
        // 取用时最大值，便于判断程序是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(FAILED);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(SUCCEED);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // 想要获取内存占用需要调用第三方库，非常麻烦，留作拓展，暂不实现
        // judgeInfo.setMemory();

        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5.文件清理
     *
     * @param userCodeFile 要删除的文件
     * @return 结果
     */
    public boolean deleteFile(File userCodeFile) {
        //  5. 文件清理，释放空间
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }


    /**
     * 获取错误响应
     *
     * @param e
     * @return 执行响应
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
