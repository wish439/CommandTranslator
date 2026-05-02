package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class TranslatableCommentAttitude implements Attitude {
    private final HashMap<String, String> comments;
    private final String defaultComment;

    public TranslatableCommentAttitude(Builder builder) {
        this.comments = builder.comments;
        this.defaultComment = builder.defaultComment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HashMap<String, String> comments;
        private String defaultComment;

        public Builder() {
            this.comments = new HashMap<>();
            this.defaultComment = "";
        }


        public Builder comment(String language, String comment) {
            comments.put(language, comment);
            return this;
        }

        public Builder defaultComment(String defaultComment) {
            this.defaultComment = defaultComment;
            return this;
        }

        public TranslatableCommentAttitude build() {
            return new TranslatableCommentAttitude(this);
        }
    }
}
