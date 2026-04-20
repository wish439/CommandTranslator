package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.TranslatableCommentAttitudeAdapter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class TranslatableCommentAttitude implements Attitude<TranslatableCommentAttitudeAdapter> {
    private final HashMap<String, String> comments;
    private final String defaultComment;
    @Override
    public Class<TranslatableCommentAttitudeAdapter> getAdapter() {
        return TranslatableCommentAttitudeAdapter.class;
    }

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
