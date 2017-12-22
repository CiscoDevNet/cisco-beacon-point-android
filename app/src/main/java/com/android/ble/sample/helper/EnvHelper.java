package com.android.ble.sample.helper;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mark Craig on 1/6/17.
 */

public class EnvHelper {

    // constants
    private static final String TAG = "EnvHelper";

    /**
     * Returns the environment for the given QR-code secret string.
     *
     * @param url URL given by the scanned QR-code
     * @return
     */
    public static String getEnvForURL(String url) {
        // build the regular expression
        Pattern p = Pattern.compile("^http[s]?:\\/\\/.*\\/(.+)");
        Matcher m = p.matcher(url);

        // if the given url matches the regex
        String env;
        if (m.find()) {
            // get the matching secret
            String secret = m.group(1);

            // get the first character of the secret
            String secretEnvType = secret.substring(0, 1);

            // set the environment string to return
            if (secretEnvType.equals("P")) {
                env = "Production";
            } else if (secretEnvType.equals("S")) {
                env = "Staging";
            } else {
                env = "Dev";
            }
        } else {
            // fallback to the development environment
            env = "Dev";

            // log the error
            Log.e(TAG, "QR-Code URL does not match regex pattern");
        }

        // return the environment string
        return env;
    }

    /**
     * Returns the topic name for the given environment
     *
     * @param env
     * @return
     */
    public static String getTopicForEnv(String env) {
        String topic;

        // determine the given environment
        if (env.equals("Production")) {
            topic = "mobile-client-production";
        } else if (env.equals("Staging")) {
            topic = "mobile-client-staging";
        } else {
            topic = "mobile-client-dev";
        }

        // return the topic name for the given environment
        return topic;
    }
}
