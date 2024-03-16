package com.karlexyan.yojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.List;

public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        // 获取默认的Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        // 1.拉取镜像
        String image = "nginx:latest";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback resultCallback = new PullImageResultCallback() {  // 命令执行回调函数
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("拉取镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd.exec(resultCallback).awaitCompletion();
        System.out.println("拉取镜像完成");

        // 2.创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        System.out.println("创建容器中");
        CreateContainerResponse response = containerCmd.withCmd("echo", "Hello Docker").exec();
        String containerId = response.getId();
        System.out.println("容器创建成功，ID为：" + containerId);

        // 3.查看容器状态
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for (Container container : containerList) {
            System.out.println("容器：" + container);
        }

        // 4.启动容器
        System.out.println("启动容器");
        dockerClient.startContainerCmd(containerId).exec();

        // 5.查看容器日志
        System.out.println("查看容器日志");
        LogContainerResultCallback LogResultCallback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                System.out.println("ID为：" + containerId + "，容器日志：" + new String(item.getPayload()));
                super.onNext(item);
            }
        };
        dockerClient.logContainerCmd(containerId)
                .withStdErr(true)  // 错误输出
                .withStdOut(true)  // 标准输出
                .exec(LogResultCallback)
                .awaitCompletion();  // 异步操作

        // 6.删除容器
        System.out.println("删除容器");
        dockerClient.removeContainerCmd(containerId)
                .withForce(true) // 强制删除
                .exec();

        // 删除所有容器
//        for (Container container : containerList) {
//            dockerClient.removeContainerCmd(container.getId())
//                    .withForce(true) // 强制删除
//                    .exec();
//        }

        // 7.删除镜像
        System.out.println("删除镜像");
        dockerClient.removeImageCmd(image).withForce(true).exec();

    }
}
