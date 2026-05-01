package com.wishtoday.ts.commandtranslator.Util;

import java.util.Arrays;
import java.util.List;

public class StringUtils {
    public static String filterUnblank(String delimiter, String s) {
        String[] split = s.split(delimiter);
        List<String> list = Arrays.stream(split)
                .filter(a -> !a.isBlank())
                .toList();
        return String.join(delimiter, list);
    }
}
