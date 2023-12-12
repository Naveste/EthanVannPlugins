package com.example.AutoTitheFarm;

import static com.example.AutoTitheFarm.AutoTitheFarmPlugin.farmingLevel;

public enum Plants {
    GOLOVANOVA(34, 53, 27384, 27387, 27390, 27393),

    BOLOGANO(54, 73, 27395, 27398, 27401, 27404),

    LOGAVANO(74, 99, 27406, 27409, 27412, 27415);

    private final int minLevelRequirement;

    private final int maxLevelRequirement;

    // unwatered IDs
    private final int firstStageId;

    private final int secondStageId;

    private final int thirdStageId;

    private final int fourthStageId;

    Plants(int minLevelRequirement, int maxLevelRequirement, int firstStageId, int secondStageId, int thirdStageId, int fourthStageId) {
        this.minLevelRequirement = minLevelRequirement;
        this.maxLevelRequirement = maxLevelRequirement;
        this.firstStageId = firstStageId;
        this.secondStageId = secondStageId;
        this.thirdStageId = thirdStageId;
        this.fourthStageId = fourthStageId;
    }

    private int getStageId(int stageId) {
        if (farmingLevel >= minLevelRequirement && farmingLevel <= maxLevelRequirement) {
            return stageId;
        }
        return -1;
    }

    public int getFirstStageId() {
        return getStageId(firstStageId);
    }

    public int getSecondStageId() {
        return getStageId(secondStageId);
    }

    public int getThirdStageId() {
        return getStageId(thirdStageId);
    }

    public int getFourthStageId() {
        return getStageId(fourthStageId);
    }
}
