package com.wishtoday.ts.commandtranslator.FunctionHandler;

import lombok.Getter;
import net.minecraft.data.DataOutput;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.ExpandedMacro;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;
import java.util.List;

public class FunctionCreator {
    private static final String FUNCTION_DIRECTORY = "function";


    @Getter
    public static class FunctionDataPack {
        private PackResourceMetadata metadata;
        private List<ExpandedMacro<?>> functions;
        //private DataOutput dataOutput;
        private Path path;
        private MinecraftServer server;

        public FunctionDataPack(PackResourceMetadata metadata, List<ExpandedMacro<?>> functions, MinecraftServer server) {
            this.metadata = metadata;
            this.functions = functions;
            this.server = server;
            //this.dataOutput = new DataOutput(server.getPath(String.valueOf(DataOutput.OutputType.DATA_PACK)));
            this.path = this.server.getPath(WorldSavePath.DATAPACKS.getRelativePath());
        }
    }
}
