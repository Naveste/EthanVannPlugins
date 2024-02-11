package com.example.AutoScurriusPrayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("AutoScurriusPrayer")
public interface AutoScurriusPrayerConfig extends Config {

    @ConfigItem(
            keyName = "offensivePrayer",
            name = "Offensive prayer",
            description = " ",
            position = 2
    )
    default OffensivePrayer offensivePrayer() {
        return OffensivePrayer.PIETY;
    }

}
