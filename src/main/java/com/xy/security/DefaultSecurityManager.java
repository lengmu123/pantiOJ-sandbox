package com.xy.security;

import java.security.Permission;

//所有权限通过
public class DefaultSecurityManager extends SecurityManager{
    //检查所有当前的权限
    @Override
    public void checkPermission(Permission perm) {
       System.out.println("默认不做任何限制");
//       super.checkPermission(perm);
    }

}
