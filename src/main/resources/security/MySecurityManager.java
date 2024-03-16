import java.security.Permission;

public class MySecurityManager extends SecurityManager {

    /**
     * 检查所有权限
     * @param perm   the requested permission.
     */
    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
    }

    /**
     * 限制读文件权限
     *
     * @param file the system-dependent file name.
     */
    @Override
    public void checkRead(String file) {
        System.out.println(file);
        if (file.contains("D:\\codeSpace\\YOJ\\yoj-code-sandbox")) {
            return;
        }
        throw new SecurityException("checkRead 权限异常：" + file);
    }

    /**
     * 限制写文件权限
     *
     * @param file the system-dependent filename.
     */
    @Override
    public void checkWrite(String file) {
//        throw new SecurityException("checkWrite 权限异常：" + file);
    }

    /**
     * 限制执行权限
     *
     * @param cmd the specified system command.
     */
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常：" + cmd);
    }

    /**
     * 限制网络连接权限
     * @param host   the host name port to connect to.
     * @param port   the protocol port to connect to.
     */
    @Override
    public void checkConnect(String host, int port) {
//        throw new SecurityException("checkConnect 权限异常：" + host + ":" + port);
    }


    /**
     * 限制删除文件权限
     * @param file   the system-dependent filename.
     */
    @Override
    public void checkDelete(String file) {
//        throw new SecurityException("checkDelete 权限异常：" + file);
    }
}
