package com.ixxc.uiot;

import android.content.Context;
import android.os.Handler;
import android.text.InputType;

public class Utils {
    public static Handler delayHandler;

    // Format camel case back to normal string
    public static String formatString(String s) {
        char first = Character.toUpperCase(s.charAt(0));
        return first + s.substring(1).replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    public static  String capitalizeFirst(String s){
        s = s.replace("_"," ");
        return String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1).toLowerCase();
    }

    // Convert dp to px
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int getInputType(String type) {
        switch (type) {
            case "JSONObject":
            case "JSONArray":
            case "GEO_JSONPoint":
            case "JSON":
            case "booleanMap":
            case "integerMap":
            case "numberMap":
            case "multivaluedTextMap":
            case "agentLink":
            case "attributeLink[]":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
            case "timestamp":
            case "timestampISO8601":
            case "dateAndTime":
            case "timeDurationISO8601":
            case "periodDurationISO8601":
            case "timeAndPeriodDurationISO8601":
            case "integer":
            case "long":
            case "bigInteger":
            case "number":
            case "bigNumber":
            case "TCP_IPPortNumber":
            case "positiveInteger":
            case "positiveNumber":
            case "negativeInteger":
            case "negativeNumber":
            case "integerByte":
            case "byte":
            case "boolean":
            case "Polling Millis":
                return InputType.TYPE_CLASS_NUMBER;
            case "attributeLink":
            case "HTTP_URL":
            case "WS_URL":
                return InputType.TYPE_TEXT_VARIATION_URI;
            case "email":
                return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }
}
