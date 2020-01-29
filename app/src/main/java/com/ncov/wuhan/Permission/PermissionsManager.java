package com.ncov.wuhan.Permission;

import android.app.Activity;

import androidx.annotation.NonNull;

public final class PermissionsManager {

    private static final String TAG = "PermissionsManager";


    private PermissionsManager(@NonNull Activity activity) {

    }

    public static PermissionsManager get(@NonNull Activity activity) {
        return new PermissionsManager(activity);
    }

    public void requestPermissions(Activity activity, String[] permissions) {
        activity.requestPermissions(permissions, 1);
    }


}
