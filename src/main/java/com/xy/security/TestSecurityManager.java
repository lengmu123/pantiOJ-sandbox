package com.xy.security;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {
    public static void main(String[] args) {
        System.setSecurityManager(new MySecurityManager());
//        List<String> strings = FileUtil.readLines("D:\\xuexiDaima\\xyoj-code-sandbox\\src\\main\\resources\\application.yml", StandardCharsets.UTF_8);
//        System.out.println(strings);
        FileUtil.writeString("hello world", "aaa", StandardCharsets.UTF_8);
    }
}
