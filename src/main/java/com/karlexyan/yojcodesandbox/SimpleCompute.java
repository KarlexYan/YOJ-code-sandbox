package com.karlexyan.yojcodesandbox;

import java.util.Scanner;

public class SimpleCompute {

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        int a = reader.nextInt(), b = reader.nextInt();
        System.out.println("结果：" + (a + b));
    }
}
