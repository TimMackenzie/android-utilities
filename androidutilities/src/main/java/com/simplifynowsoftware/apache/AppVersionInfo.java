/*
 * Copyright (C) 2015 Tim Mackenzie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.simplifynowsoftware.apache;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * AppVersionInfo provides static methods to gather Android app version info (e.g for debug logging)
 */
public class AppVersionInfo {
    public static final long INVALID_TIME_DELTA = -1;

    protected static final boolean LOGGING_ENABLED = false;

    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            if(LOGGING_ENABLED) {
                Log.e(context.getClass().getSimpleName(), "Name not found", e);
            }
        }
        return versionName;
    }

    public static int getVersionNumber(Context context) {
        int versionNumber = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            versionNumber = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            if(LOGGING_ENABLED) {
                Log.e(context.getClass().getSimpleName(), "Name not found", e);
            }
        }
        return versionNumber;
    }

    /*
     * Determine time since .APK was built, using the MANIFEST.MF datestamp
     *
     * Format if needed with e.g.
     *  SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getInstance();
     *  formatter.setTimeZone(TimeZone.getTimeZone("gmt"));
     *  String s = formatter.format(new java.util.Date(time));
     */
    public static long getAppBuildTime(final Context context) {
        long buildTime = INVALID_TIME_DELTA;

        try{
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
            buildTime = ze.getTime();
            zf.close();
        } catch(Exception e){ // NameNotFoundException, NPE, etc.
            if(LOGGING_ENABLED) {
                Log.w(context.toString(), "exception in getAppBuildTime", e);
            }
        }

        return buildTime;
    }

    // determine time, in millis, since app was installed on this device
    public static long getAppInstallTime(final Context context) {
        long installTime = INVALID_TIME_DELTA;

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            installTime = packageInfo.lastUpdateTime;
        } catch (Exception e) {
            if(LOGGING_ENABLED) {
                Log.w(context.toString(), "exception in getAppInstallTime", e);
            }
        }

        return installTime;
    }

    /*
     * Difference between build time and current time, in milliseconds.
     * If unable to determine build time, return invalid value.
     */
    public static long timeSinceBuild(final Context context) {
        long timeDelta = INVALID_TIME_DELTA;

        final long appBuildTime = getAppBuildTime(context);

        if(appBuildTime != INVALID_TIME_DELTA) {
            timeDelta = System.currentTimeMillis() - appBuildTime;
        }

        return timeDelta;
    }

    public static String getDebugVersionInfo(final Context context, final String timeZone) {
        long buildTime = getAppBuildTime(context);
        long installTime = getAppInstallTime(context);

        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getInstance();
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        String buildDate = formatter.format(new java.util.Date(buildTime));
        String installDate = formatter.format(new java.util.Date(installTime));

        StringBuilder sb = new StringBuilder();

        sb.append("Build date: ");
        sb.append(buildDate);
        sb.append("\nInstall date: ");
        sb.append(installDate);
        sb.append("\nVersion name: ");
        sb.append(getVersionName(context));
        sb.append("\nVersion number: ");
        sb.append(getVersionNumber(context));

        return sb.toString();
    }
}