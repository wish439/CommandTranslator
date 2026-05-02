package com.wishtoday.ts.commandtranslator.Config;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ConfigPathReader {
    private final String string;
    @Setter
    private int cursor;
    public ConfigPathReader(String string) {
        this.string = string;
        this.cursor = 0;
    }
    public ConfigPathReader(ConfigPathReader reader) {
        this.string = reader.string;
        this.cursor = reader.cursor;
    }

    public boolean hasNextPath() {
        int i = this.cursor;
        String s = this.readNextPath();
        boolean b = s == null || s.isEmpty();
        this.setCursor(i);
        return !b;
    }

    public String readNextPath() {
        if (this.cursor >= this.string.length()) {
            return null;
        }
        if (this.string.charAt(this.cursor) == '.') {
            this.cursor++;
        }
        int i = this.cursor;
        while (this.cursor < this.string.length()) {
            char c = this.string.charAt(this.cursor);
            if (c == '.') {
                break;
            }
            this.cursor++;
        }
        int min = Math.min(this.cursor, this.string.length());
        return this.string.substring(i, min);
    }
}
