package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude;

import lombok.Builder;

@Builder
public record RangeAttitude<N extends Number>(N min, N max) implements Attitude{
}
