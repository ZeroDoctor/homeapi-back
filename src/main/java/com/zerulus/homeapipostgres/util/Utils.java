package com.zerulus.homeapipostgres.util;

import java.util.HashMap;

public class Utils {

    public static final HashMap<Character, String> hashConvert = new HashMap<Character, String>(){{
        put(' ',"_s_");
        put('$',"_l_");
        put('#',"_h_");
        put('+',"_p_");
        put('-',"_d_");
        put('@',"_a_");
        put('(',"_lb_");
        put(')',"_rb_");
        put('&',"_n_");
    }};

    public static String replaceSym(String str) {

        StringBuilder result = new StringBuilder();

        for(int i = 0; i < str.length(); i++) {
            if(hashConvert.containsKey(str.charAt(i))) {
                result.append(hashConvert.get(str.charAt(i)));
            } else {
                result.append(str.charAt(i));
            }
        }

        return result.toString();
    }

    public static String rangeId(String id, int limit) {
        String[] split = id.split("\\.");
        if(split.length > 1) {
            StringBuilder result = new StringBuilder();
            for(int i = 0; i < split.length - limit; i++) {
                result.append(split[i]).append(".");
            }

            if(split.length - limit == 1) return split[0];
            String str = String.valueOf(result);
            return str.substring(0, str.length() - 1);
        }

        return split[0];
    }

    public static String cleanId(String target) {
        return cleanId(target, 1);
    }

    public static String cleanId(String target, int limit) {
        String tarSplit = rangeId(target, limit);
        return tarSplit.replace("+", ".");
    }
}
