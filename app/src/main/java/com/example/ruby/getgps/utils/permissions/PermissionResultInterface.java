package com.example.ruby.getgps.utils.permissions;

/**
 * Created by rubymobile on 9/27/16.
 */

public interface PermissionResultInterface {
    void successfulPermission();

    void deniedPermissions();

    void neverAskPermissions();
}
