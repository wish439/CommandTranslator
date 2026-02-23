package com.wishtoday.ts.commandtranslator.FunctionHandler;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Util.NioUtils;
import lombok.Getter;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.ExpandedMacro;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FunctionCreator {
    private static final String FUNCTION_DIRECTORY = "function";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    FunctionCreator() {

    }

    @SuppressWarnings("UnstableApiUsage")
    public void create(FunctionDataPack dataPack, MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.DATAPACKS);
        Path rootDirectoryPath = path.resolve(dataPack.name);
        NioUtils.deleteDirectories(rootDirectoryPath);
        path = rootDirectoryPath.resolve("data");
        List<ExpandedMacro<ServerCommandSource>> functions = dataPack.functions;
        if (functions.isEmpty()) {
            return;
        }
        int processors = Math.min(functions.size(), Runtime.getRuntime().availableProcessors());
        List<List<ExpandedMacro<ServerCommandSource>>> partition = Lists.partition(functions, processors);
        try (ExecutorService executor = Executors.newFixedThreadPool(processors)) {
            CompletableFuture<Void> metaFuture = CompletableFuture.runAsync(() -> {
                Path mcMetaPath = rootDirectoryPath.resolve("pack.mcmeta");
                NioUtils.createFileAndWrite(mcMetaPath, ModResourcePackUtil.serializeMetadata(dataPack.packFormat, dataPack.description));
            }, executor);
            Path finalPath = path;
            List<CompletableFuture<Void>> collect = partition.stream()
                    .map(list -> CompletableFuture.runAsync(() -> list.forEach(macro -> {
                        Identifier id = macro.id();
                        Path resolve = resolveFromID(id, finalPath);

                        NioUtils.createFileAndWrite(resolve, macro.entries().stream().map(Object::toString).collect(Collectors.joining("\n")));
                    }), executor))
                    .collect(Collectors.toList());
            collect.add(metaFuture);
            CompletableFuture.allOf(collect.toArray(new CompletableFuture[0])).join();
        }
    }

    private static Path resolveFromID(Identifier id, Path path) {
        /*path = path.resolve(id.getNamespace());
        path = path.resolve(FUNCTION_DIRECTORY);
        String path1 = id.getPath();
        String[] split = path1.split("/");
        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1) {
                path = path.resolve(split[i] + ".mcfunction");
                continue;
            }
            path = path.resolve(split[i]);
        }
        return path;*/

        return path.resolve(id.getNamespace())
                .resolve(FUNCTION_DIRECTORY)
                .resolve(id.getPath() + ".mcfunction");
    }

    public static PackResourceMetadata createEmptyMetaData(Text description) {
        return new PackResourceMetadata(description, SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA), Optional.empty());
    }

    public static PackResourceMetadata createEmptyMetaData(String description, Object... args) {
        return createEmptyMetaData(Text.of(String.format(description, args)));
    }

    @Getter
    public static class FunctionDataPack {
        private String name;
        private String description;
        private int packFormat;
        private List<ExpandedMacro<ServerCommandSource>> functions;
        //private DataOutput dataOutput;

        public FunctionDataPack(String description, List<ExpandedMacro<ServerCommandSource>> functions, String name) {
            this.description = description;
            this.packFormat = SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA);
            this.functions = functions;
            this.name = name;
            //this.dataOutput = new DataOutput(server.getPath(String.valueOf(DataOutput.OutputType.DATA_PACK)));
        }
    }
}
