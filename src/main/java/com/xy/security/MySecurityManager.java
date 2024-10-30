package com.xy.security;

import java.security.Permission;

//所有权限通过
public class MySecurityManager extends SecurityManager{
    //检查所有当前的权限
    @Override
    public void checkPermission(Permission perm) {

//       super.checkPermission(perm);
    }
    //检查程序是否允许执行
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("程序不允许执行"+cmd);
    }
    //检查程序是否允许读取
    @Override
    public void checkRead(String file) {
        System.out.println(file);
        if (file.contains("hutool")){
            return;
        }
//        throw new SecurityException("程序不允许读取"+file);
    }
    //检查程序是否允许写入
    @Override
    public void checkWrite(String file) {

//        throw new SecurityException("程序不允许写入"+file);
    }
    //检查程序是否允许删除
    @Override
    public void checkDelete(String file) {

//        throw new SecurityException("程序不允许删除"+file);
    }
    //检查程序是否允许连接
    @Override
    public void checkConnect(String host, int port) {

//        throw new SecurityException("程序不允许连接"+host+":"+port);
    }
}
