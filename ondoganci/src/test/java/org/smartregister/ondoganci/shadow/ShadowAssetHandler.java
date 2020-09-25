package org.smartregister.ondoganci.shadow;

import android.content.Context;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.smartregister.util.AssetHandler;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2020-02-06
 */

@Implements(AssetHandler.class)
public class ShadowAssetHandler {

    @Implementation
    public static String readFileFromAssetsFolder(String fileName, Context context) {
        if ("special_vaccines.json".equals(fileName)) {
            return VaccineDataUtils.SPECIAL_VACCINES_JSON;
        } else if ("vaccines.json".equals(fileName)) {
            return VaccineDataUtils.VACCINES_JSON;
        }

        return "";
    }
}
