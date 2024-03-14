package com.karlexyan.yojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeRequest;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //  1. 把代码保存为文件
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
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8); // 使用UTF-8编码写入

        //  2. 编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath()); // 编译命令
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            // 等待Process程序执行完，获取错误码
            int exitValue = compileProcess.waitFor();

            if (exitValue == 0) {  // 正常退出
                System.out.println("编译成功");
                // 通过进程获取正常输出到控制台的信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                System.out.println(compileOutputStringBuilder); // 输出控制台信息

            } else { // 异常退出
                System.out.println("编译失败，错误码：" + exitValue);
                // 通过进程获取正常输出到控制台的信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                // 分批获取进程的错误输出
                BufferedReader errorBufferReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                // 逐行读取错误信息
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferReader.readLine()) != null) {
                    errorCompileOutputStringBuilder.append(errorCompileOutputLine);
                }
                System.out.println(compileOutputStringBuilder); // 打印正常输出信息
                System.out.println(errorCompileOutputStringBuilder); // 打印异常输出信息
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        //  3. 执行代码，得到输出结果
        //  4. 收集整理输出结果
        //  5. 文件清理，释放空间
        //  6. 错误处理，提升程序健壮性
        return null;
    }
}
