package hello.world.fpt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by USER on 4/21/2021.
 */

public class Ultis {
    public static void saveKey(Activity activity, String key, String value) {
        if (key.isEmpty()) return;
        SharedPreferences settings = activity.getSharedPreferences(NPNConstants.SETTING_REFKEY_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String loadKey(Activity activity, String key) {
        SharedPreferences settings = activity.getSharedPreferences(NPNConstants.SETTING_REFKEY_NAME, Context.MODE_PRIVATE);

        if (key.equals(NPNConstants.SETTING_KEY_ID_STATION)) {
            return settings.getString(key, NPNConstants.SETTING_ID_STATION + "");
        } else {
            return settings.getString(key, "0");
        }
    }
}
