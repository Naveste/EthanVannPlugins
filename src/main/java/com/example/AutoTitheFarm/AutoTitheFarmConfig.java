package com.example.AutoTitheFarm;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("AutoTitheFarm")
public interface AutoTitheFarmConfig extends Config {

    @ConfigItem(
            keyName = "layout",
            name = "Patch Layout",
            description = " ",
            position = 0
    )
    default PatchLayout patchLayout() {
        return PatchLayout.LOW_PING_LAYOUT;
    }

    @ConfigItem(
            keyName = "minRunEnergyToIdleUnder",
            name = "Idle if run energy is under",
            description = " ",
            position = 1
    )
    default int minRunEnergyToIdleUnder() {
        return 20;
    }
}
