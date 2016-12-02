package com.example.ruby.getgps.utils.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inner implementation of a tekton labs instance holding the state of the permissions request
 */
final class TektonLabsInstance {

    private final int PERMISSIONS_REQUEST_CODE = 42;

    private final Context context;
    private final AndroidPermissionService androidPermissionService;
    private final Activity activity;
    private final ViewGroup rootView;
    private final List<String> neverAskAgain;

    public TektonLabsInstance(Activity activity, AndroidPermissionService androidPermissionService) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.androidPermissionService = androidPermissionService;
        this.rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        this.neverAskAgain = new ArrayList<>();


    }

    /**
     * Checks the state of a specific permission reporting it when ready to the listener.
     * .
     *
     * @param permission       One of the values found in {@link Manifest.permission}
     * @param rationaleMessage Message that will show when we need to explain permissions needed.
     * @param permGranted      Interface that execute its method when permission is already granted
     */

    void checkSinglePermission(final String permission, String rationaleMessage, PermissionGranted permGranted) {

        int permissionGranted = androidPermissionService.checkSelfPermission(context, permission);
        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            String perm;
            if (Constants.PERMISSIONS_SCOOPED.containsKey(permission) && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N){
                perm = Manifest.permission.READ_EXTERNAL_STORAGE;
            }else {
                perm = permission;
            }
            if (androidPermissionService.shouldShowRequestPermissionRationale(activity, perm)) {
                showDialog(rationaleMessage, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Constants.PERMISSIONS_SCOOPED.containsKey(permission)) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                StorageManager sm = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
                                StorageVolume volume = null;
                                volume = sm.getPrimaryStorageVolume();
                                Intent intent = volume.createAccessIntent(permission);
                                activity.startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
                            }else {
                                androidPermissionService.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                            }

                        }else {
                            androidPermissionService.requestPermissions(activity, new String[]{permission}, PERMISSIONS_REQUEST_CODE);

                        }

                    }
                }).show();

            } else {
                if (Constants.PERMISSIONS_SCOOPED.containsKey(permission) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    StorageManager sm = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
                    StorageVolume volume = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        volume = sm.getPrimaryStorageVolume();
                        Intent intent = volume.createAccessIntent(permission);
                        activity.startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
                    }
                }else {
                    androidPermissionService.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

                }
            }
        } else {
            permGranted.alreadyGranted();
        }


    }

    /**
     * Checks the state of a group of permissions reporting it when ready to the listener.
     * .
     *
     * @param permissions                Values found in {@link Manifest.permission}
     * @param rationaleMessage           Message that will show when we need to explain permissions needed.
     * @param showPermissionsOnRationale Value that validate if we show the permissions requested
     *                                   when explaining.
     * @param allGranted                 Interface that execute its method when permission is already granted
     */
    void checkMultiplePermissions(final List<String> permissions, String rationaleMessage, boolean showPermissionsOnRationale, PermissionGranted allGranted) {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionScooped = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<String>();
        for (String permission : permissions) {
            if (Constants.PERMISSIONS_SCOOPED.containsKey(permission)){
                    permissionsNeeded.add(Constants.PERMISSIONS_SCOOPED.get(permission));
                    permissionScooped.add(Constants.PERMISSIONS_SCOOPED.get(permission));
            }else {
                if (addPermission(permissionsList, permission)) {
                    permissionsNeeded.add(Constants.PERMISSIONS_MAP.get(permission));
                }
            }

        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = rationaleMessage;
                if (showPermissionsOnRationale) {
                    String permissionsAsked = permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        permissionsAsked = permissionsAsked + ", " + permissionsNeeded.get(i);
                    message = message + "( " + permissionsAsked + " ).";
                }
                showDialog(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        androidPermissionService.requestPermissions(activity,
                                permissionsList.toArray(new String[permissionsList.size()]),
                                PERMISSIONS_REQUEST_CODE);
                        if (permissionScooped.size() > 0 ){
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                StorageManager sm = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
                                StorageVolume volume = null;
                                volume = sm.getPrimaryStorageVolume();
                                for (String perm : permissionScooped){
                                    Intent intent = volume.createAccessIntent(perm);
                                    activity.startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
                                }
                            }else {
                                androidPermissionService.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                            }
                        }
                    }
                }).show();

                return;
            }
            androidPermissionService.requestPermissions(activity,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    PERMISSIONS_REQUEST_CODE);
            if (permissionScooped.size() > 0 ){

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    StorageManager sm = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
                    StorageVolume volume = null;
                    volume = sm.getPrimaryStorageVolume();
                    for (String perm : permissionScooped){
                        Intent intent = volume.createAccessIntent(perm);
                        activity.startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
                    }
                }

            }
        } else {
            allGranted.alreadyGranted();
        }
    }

    /**
     * This method will check if a permission need a rationale message to the user.
     * .
     *
     * @param permissionsList The list of permissions that are going to be requested if needed.
     * @param permission      The permission which will be checked if it is granted or not.
     */

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (androidPermissionService.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!androidPermissionService.shouldShowRequestPermissionRationale(activity, permission) )
                return false;
        }
        return true;
    }

    /**
     * This method needs to be used in onRequestPermissionsResult to check if user marked Never ask
     * again in requested permissions.
     * .
     *
     * @param requestCode     The request code passed in onRequestPermissionsResult
     * @param permissions     The requested permissions. Never null.
     * @param grantResults    The grant results for the corresponding permissions
     */

    void ActivityPermissionsResults(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, PermissionResultInterface response) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            neverAskAgain.clear();
            Map<String, Integer> perms = new HashMap<String, Integer>();
            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i], grantResults[i]);
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (!androidPermissionService.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                        neverAskAgain.add(permissions[i]);
                    }
                }

            }
            if (!neverAskAgain.isEmpty()) {
                response.neverAskPermissions();
            } else {
                if (!perms.containsValue(PackageManager.PERMISSION_DENIED)) {
                    response.successfulPermission();
                } else {
                    response.deniedPermissions();
                }
            }

        }
    }

    private Dialog showDialog(String message, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton(Constants.OK_BUTTON, okListener)
                .setNegativeButton(Constants.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setMessage(message);

        return builder.create();
    }


    int getRequestCode() {
        return PERMISSIONS_REQUEST_CODE;
    }


}
