package com.wishtoday.ts.commandtranslator.Util;

//TODO: Move this class to other package.
public class LanguageUtils {
    public static boolean isChineseSentence(String sentence, double range) {
        if (range < 0) {
            return false;
        }
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

        return ((double) han / total) > range;
    }

    public static boolean isOnlySymbols(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (isSemanticCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSemanticCharacter(char c) {
        if (Character.isLetterOrDigit(c)) {
            return true;
        }

        Character.UnicodeScript script = Character.UnicodeScript.of(c);
        return script == Character.UnicodeScript.HAN
                || script == Character.UnicodeScript.HIRAGANA
                || script == Character.UnicodeScript.KATAKANA
                || script == Character.UnicodeScript.HANGUL;
    }
}
