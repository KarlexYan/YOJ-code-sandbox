package com.karlexyan.yojcodesandbox.utils;

import com.karlexyan.yojcodesandbox.model.ExecuteMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 进程工具类
 */
public class ProcessUtils {

    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String processState) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            // 等待Process程序执行完，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);

            if (exitValue == 0) {  // 正常退出
                System.out.println(processState + "成功");
                // 通过进程获取正常输出到控制台的信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());  // 填入控制台信息

            } else { // 异常退出
                System.out.println(processState + "失败，错误码：" + exitValue);
                // 通过进程获取正常输出到控制台的信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());  // 填入控制台信息

                // 分批获取进程的错误输出
                BufferedReader errorBufferReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                // 逐行读取错误信息
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferReader.readLine()) != null) {
                    errorCompileOutputStringBuilder.append(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());  // 填入错误
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}
