package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.AttitudeAdapter;

public interface Attitude<A extends AttitudeAdapter<?>> {
    Class<A> getAdapter();
}
