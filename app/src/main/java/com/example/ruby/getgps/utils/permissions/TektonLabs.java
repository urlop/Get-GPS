package com.example.ruby.getgps.utils.permissions;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Created by rubymobile on 8/4/16.
 */
public class TektonLabs {

    private static TektonLabsInstance instance;
    public static int PERMISSIONS_REQUEST_CODE;

    public static void initialize(Activity activity){
        if (instance == null){
            AndroidPermissionService androidPermissionService = new AndroidPermissionService();
            instance = new TektonLabsInstance(activity,androidPermissionService);
            PERMISSIONS_REQUEST_CODE = instance.getRequestCode();
        }

    }
    /**
     * Checks the state of a specific permission reporting it when ready to the listener.
     * .
     *
     * @param permission One of the values found in {@link android.Manifest.permission}
     * @param rationaleMessage Message that will show when we need to explain permissions needed.
     */
    public static void checkPermission(String permission, String rationaleMessage, PermissionGranted permissionGranted){
        checkInstanceIsNotNull();
        instance.checkSinglePermission(permission, rationaleMessage,permissionGranted);
    }

    /**
     * Checks the state of a group of permissions reporting it when ready to the listener.
     * .
     *
     * @param permissions Values found in {@link android.Manifest.permission}
     * @param rationaleMessage Message that will show when we need to explain permissions needed.
     * @param showPermissionOnRationale Value that validate if we show the permissions requested
     *                                   when explaining.
     */
    public static void checkPermissions(String rationaleMessage,boolean showPermissionOnRationale,PermissionGranted permissionGranted, String... permissions){
        checkInstanceIsNotNull();
        instance.checkMultiplePermissions(Arrays.asList(permissions),rationaleMessage,showPermissionOnRationale,permissionGranted);

    }

    /**
     * This method needs to be used in onRequestPermissionsResult to check if user marked Never ask
     * again in requested permissions.
     * .
     *
     * @param requestCode The request code passed in onRequestPermissionsResult
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     */
    public static void activityPermissionsResults(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, PermissionResultInterface permissionResultInterface){
        checkInstanceIsNotNull();
        instance.ActivityPermissionsResults(requestCode,permissions,grantResults,permissionResultInterface);
    }


    private static void checkInstanceIsNotNull(){
        if (instance == null){
            throw new NullPointerException("context == null \n Must call \"initialize\" on TektonLabs");
        }
    }


}
