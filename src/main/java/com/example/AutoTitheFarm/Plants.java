package com.example.AutoTitheFarm;

import com.example.EthanApiPlugin.Collections.Inventory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.AutoTitheFarm.AutoTitheFarmPlugin.*;

@Slf4j
@AllArgsConstructor
public enum Plants {
    GOLOVANOVA("Golovanova", 34, 53, 27384, 27387, 27390, 27393),

    BOLOGANO("Bologano", 54, 73, 27395, 27398, 27401, 27404),

    LOGAVANO("Logavano", 74, 99, 27406, 27409, 27412, 27415);

    @Getter(AccessLevel.PACKAGE)
    private final String plantName;

    private final int minLevelRequirement;

    private final int maxLevelRequirement;

    // unwatered IDs
    @Getter(AccessLevel.PACKAGE)
    private final int firstStageId;

    @Getter(AccessLevel.PACKAGE)
    private final int secondStageId;

    @Getter(AccessLevel.PACKAGE)
    private final int thirdStageId;

    @Getter(AccessLevel.PACKAGE)
    private final int fourthStageId;

    private boolean isFarmingLevelInRange() {
        return getFarmingLevel() >= minLevelRequirement && getFarmingLevel() <= maxLevelRequirement;
    }

    private Plants getPlant() {
        return isFarmingLevelInRange() ? this : null;
    }

    static Plants getNeededPlant() {
        Plants neededPlant = null;
        for (Plants plant : Plants.values()) {
            if (plant.getPlant() == null) {
                continue;
            }
            neededPlant = plant.getPlant();
        }
        return neededPlant;
    }

    static Widget getSeed() {
        return Inventory.search().withName(getNeededPlant().getPlantName() + " seed").first().orElse(null);
    }

    static boolean isCurrentSeedMatchingFarmingLevel() {
        String currentSeed;
        Pattern pattern = Pattern.compile("<col=ff9040>(\\w+)");
        Matcher matcher = null;

        try {
            matcher = pattern.matcher(getSeed().getName());
        } catch (NullPointerException e) {
            log.info(e.getMessage());
        }

        assert matcher != null;
        currentSeed = matcher.find() ? matcher.group(1) : null;

        String compareString = getNeededPlant().getPlantName();

        // current seed in the inventory should never be null either way
        assert currentSeed != null;
        return currentSeed.equals(compareString);
    }
}
