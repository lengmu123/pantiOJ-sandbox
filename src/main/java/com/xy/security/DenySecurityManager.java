package com.xy.security;

import java.security.Permission;

//所有权限拒绝
public class DenySecurityManager extends SecurityManager{
    //检查所有当前的权限
    @Override
    public void checkPermission(Permission perm) {
       throw new SecurityException("权限不足"+perm.getActions());
    }

}
