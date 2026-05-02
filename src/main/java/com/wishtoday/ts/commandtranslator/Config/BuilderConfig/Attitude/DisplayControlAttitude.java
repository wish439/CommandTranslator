package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude;

import java.util.HashSet;
import java.util.Set;

public record DisplayControlAttitude(String onlyDisplay, Set<String> notDisplay) implements Attitude {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String onlyDisplay;
        private Set<String> notDisplay;

        public Builder() {
            this.notDisplay = new HashSet<>();
        }


        public Builder onlyDisplay(String onlyDisplay) {
            this.onlyDisplay = onlyDisplay;
            return this;
        }

        public Builder notDisplay(String notDisplay) {
            this.notDisplay.add(notDisplay);
            return this;
        }

        public DisplayControlAttitude build() {
            return new DisplayControlAttitude(onlyDisplay, notDisplay);
        }
    }
}
