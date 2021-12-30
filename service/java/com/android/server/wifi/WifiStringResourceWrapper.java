/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.server.wifi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.telephony.SubscriptionManager;
import android.util.Log;

/**
 * Provide a wrapper to reading string overlay resources for WiFi.
 *
 * Specifically intended to provide a mechanism to store and read carrier-specific translatable
 * string overlays. Carrier-specific (MVNO) overlays are not supported - but Carrier Configurations
 * which do support MVNOs do not support translatable strings.
 *
 * The WiFi-stack specific solution is to store the strings in the general name-space with a known
 * prefix.
 */
public class WifiStringResourceWrapper {
    private static final String TAG = "WifiStringResourceWrapper";

    private final WifiContext mContext;
    private final int mSubId;
    private final Resources mResources;

    /**
     * @param context a WifiContext
     * @param subId   the sub ID to use for all the resources (overlays or carrier ID)
     */
    WifiStringResourceWrapper(WifiContext context, int subId) {
        mContext = context;
        mSubId = subId;

        mResources = getResourcesForSubId();
    }

    /**
     * Returns the string corresponding to the resource ID - or null if no resources exist.
     */
    public String getString(String name, Object... args) {
        if (mResources == null) return null;
        int resourceId = mResources.getIdentifier(name, "string",
                mContext.getWifiOverlayApkPkgName());
        if (resourceId == 0) return null;

        try {
            return mResources.getString(resourceId, args);
        } catch (java.util.IllegalFormatException e) {
            Log.e(TAG, "Resource formatting error - '" + name + "' - " + e);
            return null;
        }
    }

    /**
     * Returns the resources from the given context for the MCC/MNC
     * associated with the subscription.
     */
    private Resources getResourcesForSubId() {
        try {
            Context resourceContext = mContext.createPackageContext(
                    mContext.getWifiOverlayApkPkgName(), 0);
            return SubscriptionManager.getResourcesForSubId(resourceContext, mSubId);
        } catch (PackageManager.NameNotFoundException ex) {
            return null;
        }
    }
}
