package someapp.autoupdate.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * This class has handler functions for notifications received from FCM
 * It downloads app apk and sends broadcast to target app.
 * @author Prabhat Sharma
 */

public class NotificationHandler {
    private final String TAG = "NotificationHandler";
    private final String INTENT_NAME = "UPDATE_EVENT";
    private final String VERSION_KEY = "AvailableVersion";
    private final String MANDATORY_KEY = "IsUpdateMandatory";
    private Context context;
    private  PackageManager packageManager;

    public NotificationHandler(Context context){
        this.context = context;
        packageManager = context.getPackageManager();
    }

    /**
     * Handler method for notifications received from FCM
     * @param data
     */
    public void handleNotification(Map<String, String> data){
        String packageName = "";
        String latestVersion = "";
        String isMandatory = "";
        String appName = "";
        String downloadURL = "";

        try {
            packageName = data.get("packageName");
            latestVersion = data.get("latestVersion");
            isMandatory = data.get("isMandatory");
            appName = data.get("appName");
            downloadURL = data.get("url");

        }catch (Exception e){
            Log.e(TAG,"err retrieving data: " + e.getMessage());
        }

        boolean isAppInstalled = checkAppAvailability(packageName);
        if(isAppInstalled) {
            boolean isUpdateReq = checkVersion(packageName, latestVersion);
            if(isUpdateReq)
                downloadAPK(downloadURL, appName, latestVersion, packageName);
            else
                Log.i(TAG,"No update reqd for app: " + packageName);
        }
        else {
            Log.d(TAG, "No such app exists on the device");
        }
    }

    /**
     * This method checks if a given package is installed on the device
     * @param packageName package name of an app.
     * @return a boolean representing whether the package is installed on the device or not.
     */
    private boolean checkAppAvailability(String packageName)
    {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package : " + packageName + " not installed");
            return false;
        }
    }

    /**
     * This method checks if a given app requires an update or not by comparing the latest available
     * version and current version installed on the device.
     * @param packageName package name of the app
     * @param latestVersion latest available version of the app
     * @return whether update is required for an app or not.
     */
    private boolean checkVersion(String packageName, String latestVersion){

        try {
            long currentVersion;
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
                currentVersion = packageInfo.getLongVersionCode();
            else
                currentVersion = (long)packageInfo.versionCode;
            long latestVer = Long.parseLong(latestVersion);
            return latestVer > currentVersion ? true : false;

        }catch (Exception e) {
            Log.e(TAG, "error checking version : " + e.getMessage());
            return false;
        }
    }

    /**
     * This method downloads the updated apk to an external folder.
     * @param url url for downloading the apk
     * @param appName name of the app
     * @param latestVersion the latest available version
     * @param packageName package name of the app
     */
    private void downloadAPK(final String url,final String appName,
                             final String latestVersion, final String packageName) {

        Log.i(TAG, "Update available for app : " + appName + ", starting download");
        String subPath = appName + ".apk";
        final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + File.separator + subPath;
        final File tempFile = new File(filePath);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.copyURLToFile(new URL(url), tempFile);
                        sendBroadcastIntent(packageName, latestVersion, filePath, true);
                    } catch (Exception e) {
                        Log.e(TAG, "err downloading: " + e.getMessage());
                        //for anything extra to do, add onDownloadFailed function
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Method for sending a broadcast intent to the target app.
     * @param packageName package name of the target app
     * @param filePath location of the apk on device
     * @param latestVersion latest available version of target app.
     * @param isMandatory whether update is mandatory or not.
     */
    public void sendBroadcastIntent(String packageName, String latestVersion, String filePath,
                              boolean isMandatory){
        try {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(INTENT_NAME);
            broadcastIntent.setPackage(packageName);
            broadcastIntent.putExtra(VERSION_KEY, latestVersion);
            broadcastIntent.putExtra(MANDATORY_KEY, String.valueOf(isMandatory));
            broadcastIntent.putExtra("filePath", filePath);
            context.sendBroadcast(broadcastIntent);
        }catch (Exception e){
            Log.e(TAG, "Error in sending broadcast" + e);
        }
    }

}
