package org.openinit.multicloudfilemanager.util;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * CrashLogger utility providing logging helpers for non-fatal errors.
 * External non-FOSS telemetry services (e.g. Microsoft AppCenter) have been removed.
 */
public class CrashLogger {

    private static final String TAG = "CrashLogger";

    public static void initCrashLogging(@NonNull Context context) {
        FLog.d(TAG, "Crash logging initialized (local logging mode)");
    }

    public static void logNonFatal(@NonNull String tag, @NonNull String message, @NonNull Throwable e) {
        FLog.e(tag, message, e);
    }
}
