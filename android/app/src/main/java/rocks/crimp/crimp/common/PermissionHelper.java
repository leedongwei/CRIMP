package rocks.crimp.crimp.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PermissionHelper {
    /**
     * Return a subset of {@code permissionRequired} which are not granted permission.
     * @param context context
     * @param permissionsRequired list of permissionse required
     * @return array of permissions that are required but are not granted permission
     */
    public static String[] checkPermissionsLack(Context context,
                                                @NonNull String[] permissionsRequired){
        List<String> permissionsLack = new ArrayList<>();

        for(String permission:permissionsRequired){
            if(ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED){
                permissionsLack.add(permission);
            }
        }

        return permissionsLack.toArray(new String[permissionsLack.size()]);
    }

    /**
     * Return a list of boolean value indicating whether each permission in {@code permissionList}
     * has been granted permission.
     * @param sharedPreferences {@link SharedPreferences} containing if permission has been granted
     * @param permissions list of permissions to check
     * @return list of boolean value indicating whether each permission in {@code permissionList}
     * has been granted permission.
     */
    public static boolean[] checkPermissionAskedBefore(@NonNull SharedPreferences sharedPreferences,
                                                       @NonNull String[] permissions){
        boolean[] askedBeforeList = new boolean[permissions.length];

        for(int i=0; i<permissions.length; i++){
            askedBeforeList[i] = sharedPreferences.getBoolean(permissions[i], false);
        }

        return askedBeforeList;
    }

    /**
     * Return an array of boolean indicating whether we should show rationale for requesting
     * permissions in {@code permissionList}. A value of false will happen in two scenario:
     * 1) This permission has not been asked before
     * 2) This permission was asked for at least two times and user check the "Don't ask again"
     * checkbox.
     * @param activity the target activity
     * @param permissions list of permissions to check
     * @return array of boolean indicating whether we should show rationale for requesting
     * permissions in {@code permissionList}
     */
    public static boolean[] checkShouldShowRationaleList(Activity activity,
                                                         @NonNull String[] permissions){
        boolean[] shouldShowRationaleList = new boolean[permissions.length];

        for(int i = 0; i< permissions.length; i++){
            String permission = permissions[i];
            shouldShowRationaleList[i] =
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }

        return shouldShowRationaleList;
    }

    /**
     * Request for permissions and save a boolean flag to indicate we have asked for these
     * permissions before.
     * @param sharedPreferences {@link SharedPreferences} to save boolean flag.
     * @param activity The target activity.
     * @param permissions The requested permissions.
     * @param requestCode Application specific request code to match with a result reported to
     * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}.
     */
    public static void requestPermissions(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull Activity activity, @NonNull String[] permissions,
                                          int requestCode){
        if(permissions == null || permissions.length == 0){
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(String permission:permissions){
            editor.putBoolean(permission, true);
        }
        editor.apply();
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}
