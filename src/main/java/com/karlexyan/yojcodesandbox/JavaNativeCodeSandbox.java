package com.karlexyan.yojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeRequest;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Java原生代码沙箱实现（调用模版方法）
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate{

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
