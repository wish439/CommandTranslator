package com.wishtoday.ts.commandtranslator.Services;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CommandNodeFinder {
    public static SelectStateImpl withContext(CommandContextBuilder<ServerCommandSource> context) {
        return new SelectStateImpl(context);
    }

    public static SelectStateImpl withContext(ParseResults<ServerCommandSource> context) {
        return new SelectStateImpl(context);
    }

    public interface SelectState {
        <A, B> EitherState<A, B> either(Class<A> aClass, Class<B> bClass);
    }

    public interface EitherState<A, B> {
        StartState<A, B> start();
    }

    public interface StartState<A, B> {
        boolean hasPresent();

        boolean leftPresent();

        boolean rightPresent();

        @Nullable
        ArgumentEntry<A> getLeft();

        @Nullable
        ArgumentEntry<B> getRight();
    }

    public static class SelectStateImpl implements SelectState {
        private CommandContextBuilder<ServerCommandSource> context;

        public SelectStateImpl(CommandContextBuilder<ServerCommandSource> context) {
            this.context = context;
        }

        public SelectStateImpl(ParseResults<ServerCommandSource> context) {
            this(context.getContext());
        }

        @Override
        public <A, B> EitherState<A, B> either(Class<A> aClass, Class<B> bClass) {
            return new EitherArgument<>(aClass, bClass, context);
        }
    }

    public static class EitherArgument<A, B> implements EitherState<A, B>, StartState<A, B> {
        private final Class<A> first;
        private final Class<B> second;
        private ArgumentEntry<A> left;
        private ArgumentEntry<B> right;
        private CommandContextBuilder<ServerCommandSource> context;

        public EitherArgument(Class<A> first, Class<B> second, CommandContextBuilder<ServerCommandSource> context) {
            this.first = first;
            this.second = second;
            this.context = context;
        }

        private void changeToChildestCommand() {
            CommandContextBuilder<ServerCommandSource> ctx = this.context;
            while (ctx.getChild() != null) {
                ctx = ctx.getChild();
            }
            this.context = ctx;
        }

        @Override
        public StartState<A, B> start() {
            changeToChildestCommand();
            Map<String, ParsedArgument<ServerCommandSource, ?>> arguments = context.getArguments();
            for (Map.Entry<String, ParsedArgument<ServerCommandSource, ?>> entry : arguments.entrySet()) {
                ParsedArgument<ServerCommandSource, ?> value = entry.getValue();
                Object result = value.getResult();
                if (first.isInstance(result)) {
                    A a = first.cast(result);
                    this.left = new ArgumentEntry<>(entry.getKey(), value.getRange(), a);
                    return this;
                }
                if (second.isInstance(result)) {
                    B b = second.cast(result);
                    this.right = new ArgumentEntry<>(entry.getKey(), value.getRange(), b);
                    return this;
                }
            }
            return this;
        }

        @Override
        public boolean hasPresent() {
            return leftPresent() || rightPresent();
        }

        @Override
        public boolean leftPresent() {
            return this.left != null;
        }

        @Override
        public boolean rightPresent() {
            return this.right != null;
        }

        @Override
        public @Nullable ArgumentEntry<A> getLeft() {
            return this.left;
        }

        @Override
        public @Nullable ArgumentEntry<B> getRight() {
            return this.right;
        }
    }

    public record ArgumentEntry<R>(String nodeName, StringRange range, R result) {
    }
}
