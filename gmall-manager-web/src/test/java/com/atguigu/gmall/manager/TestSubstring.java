package com.atguigu.gmall.manager;

public class TestSubstring {
    public static void main(String[] args) {
        String url="abc.abc.pear.jpg";
        int i = url.lastIndexOf(".");
        String substring = url.substring(i + 1);
        System.out.println(substring);
    }
}
