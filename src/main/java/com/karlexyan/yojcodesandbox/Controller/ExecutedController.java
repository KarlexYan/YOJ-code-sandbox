package com.karlexyan.yojcodesandbox.Controller;

import com.karlexyan.yojcodesandbox.JavaDockerCodeSandbox;
import com.karlexyan.yojcodesandbox.JavaNativeCodeSandbox;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeRequest;
import com.karlexyan.yojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.karlexyan.yojcodesandbox.constants.AuthRequest.AUTH_REQUEST_HEADER;
import static com.karlexyan.yojcodesandbox.constants.AuthRequest.AUTH_REQUEST_SECRET;

@RestController
@RequestMapping("/")
public class ExecutedController {
    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    /**
     * 接口测试
     *
     * @return
     */
    @GetMapping("/hello")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码接口
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        // 基本的认证
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            // 不匹配则禁止
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数错误");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
