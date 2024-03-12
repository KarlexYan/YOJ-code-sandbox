package com.karlexyan.yojcodesandbox;

import com.karlexyan.yojcodesandbox.model.ExecuteCodeRequest;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {

    /**
     * 代码沙箱执行代码接口
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
