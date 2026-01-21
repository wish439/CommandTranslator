package com.wishtoday.ts.commandtranslator.Reader;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wishtoday.ts.commandtranslator.Commandtranslator.LOGGER;

public class ArgumentReader {
    private static final char DEFAULT_FAILED_RETURN = '\0';
    private final String argumentString;
    private int cursor;
    private StringReader stringReader;
    public ArgumentReader(String argumentString) {
        this.argumentString = argumentString;
        this.stringReader = new StringReader(argumentString);
        this.cursor = 0;
    }
    public void skip() {
        this.cursor++;
    }

    @Nullable
    public SelectorStringInfo readSelector() {
        boolean hasSelector = false;
        while (this.cursor < this.argumentString.length() && !hasSelector) {
            char peek = peek();
            Token token = Token.get(peek);
            if (token == Token.AT) {
                hasSelector = true;
                break;
            }
            skip();
        }
        if (!hasSelector) return null;
        return readSelector0();
    }
    //@[...]
    private SelectorStringInfo readSelector0() {
        Token token = Token.get(peek());
        if (token != Token.AT) return null;
        int start = this.cursor;
        this.cursor++;
        char c = peek();
        if (c != 'a' && c != 'r' && c != 'e' && c != 'p' && c != 's') return null;
        this.cursor++;
        Token token1 = Token.get(peek());
        if (token1 != Token.LEFT_BRACKET) {
            return new SelectorStringInfo(this.argumentString.substring(start, this.cursor), start, this.cursor);
        }
        int depth = 1;
        int end = -1;
        while (this.cursor < this.argumentString.length()) {
            skip();
            char peek = peek();
            Token token2 = Token.get(peek);
            if (token2 == null) {
                continue;
            }
            if (token2 == Token.LEFT_BRACKET) {
                depth++;
                continue;
            }
            if (token2 == Token.RIGHT_BRACKET) {
                depth--;
                if (depth == 0) {
                    end = this.cursor;
                    break;
                }
                continue;
            }
        }
        if (end == -1) return null;
        return new SelectorStringInfo(this.argumentString.substring(start, end + 1), start, end + 1);
    }

    public int readInt() {
        return this.readInt(0);
    }

    public int readInt(int defaultValue) {
        try {
            return this.stringReader.readInt();
        } catch (CommandSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        return defaultValue;
    }

    public long readLong() {
        return this.readLong(0L);
    }

    public long readLong(long defaultValue) {
        try {
            return this.stringReader.readLong();
        } catch (CommandSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        return defaultValue;
    }

    public double readDouble() {
        return this.readDouble(0D);
    }

    public double readDouble(double defaultValue) {
        try {
            return this.stringReader.readDouble();
        } catch (CommandSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        return defaultValue;
    }

    public boolean readBoolean() {
        return this.readBoolean(false);
    }

    public boolean readBoolean(boolean defaultValue) {
        try {
            return this.stringReader.readBoolean();
        } catch (CommandSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        return defaultValue;
    }

    public float readFloat() {
        return this.readFloat(0F);
    }

    public float readFloat(float defaultValue) {
        try {
            return this.stringReader.readFloat();
        } catch (CommandSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        return defaultValue;
    }

    public void reset() {
        this.cursor = 0;
    }

    public boolean shouldRead(char character) {
        return Token.contain(character);
    }

    public char peek() {
        if (this.cursor < 0 || this.cursor >= this.argumentString.length()) return DEFAULT_FAILED_RETURN;
        return this.argumentString.charAt(cursor);
    }
    enum Token {
        SPACE(' '),
        AT('@'),
        LEFT_BRACKET('['),
        RIGHT_BRACKET(']'),
        LEFT_BRACE('{'),
        RIGHT_BRACE('}');
        private char character;
        private static final Map<Character, Token> tokenMap = new HashMap<>();
        Token(char character) {
            this.character = character;
        }
        public char getCharacter() {
            return this.character;
        }
        static {
            for (Token token : Token.values()) {
                tokenMap.put(token.getCharacter(), token);
            }
        }
        public static boolean contain(char character) {
            return tokenMap.containsKey(character);
        }
        public static Token get(char character) {
            return tokenMap.get(character);
        }
    }

    public record SelectorStringInfo(String selectorString, int start, int end) {
    }
}
