package com.wishtoday.ts.commandtranslator.Util;

public class LanguageUtils {
    public static boolean isChineseSentence(String sentence) {

        int han = 0;
        int total = 0;

        for (int i = 0; i < sentence.length(); i++) {
            char c = sentence.charAt(i);

            if (Character.isWhitespace(c)) continue;
            if (!Character.isLetter(c)) continue;

            switch (Character.UnicodeScript.of(c)) {
                case HIRAGANA, KATAKANA, HANGUL -> {
                    return false;
                }
                case HAN -> han++;
            }
            total++;
        }

        if (total == 0) return false;

        return ((double) han / total) > 0.6;
    }
}
