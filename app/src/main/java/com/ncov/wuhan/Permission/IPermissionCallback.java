package com.ncov.wuhan.Permission;

/**
 * Author silence.
 * Time：2019-12-05.
 * Desc：
 */
public interface IPermissionCallback {

    void onGranted();

    /**
     * 拒绝权限
     *
     * @param shouldTip      用户是否选择了不在申请弹窗
     * @param denyPermission 拒绝的权限
     */
    void onDenied(boolean shouldTip, String[] denyPermission);

}
