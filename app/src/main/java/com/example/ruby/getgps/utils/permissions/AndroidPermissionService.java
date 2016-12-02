package com.example.ruby.getgps.utils.permissions;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by rubymobile on 8/4/16.
 */
public class AndroidPermissionService {

    public AndroidPermissionService() {
    }

    /**
     * @see ContextCompat#checkSelfPermission
     */
    int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    /**
     * @see ActivityCompat#requestPermissions
     */
    void requestPermissions(@NonNull Activity activity, @NonNull String[] permissions,
                            int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * @see ActivityCompat#shouldShowRequestPermissionRationale
     */
    boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                 @NonNull String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
