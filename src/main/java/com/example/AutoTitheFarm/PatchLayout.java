package com.example.AutoTitheFarm;


import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import static com.example.PacketUtils.PacketReflection.client;

@Getter
public enum PatchLayout {
    LOW_PING_LAYOUT(
            new int[]{62, 64},
            new int[][]{
            {64, 65}, {59, 65}, {64, 62}, {59, 62}, {64, 59}, {59, 59}, {64, 56}, {59, 56}, {59, 50}, {64, 50},
            {59, 47}, {64, 47}, {59, 44}, {64, 44}, {59, 41}, {64, 41}, {69, 41}, {69, 44}, {69, 47}, {69, 50},
            {69, 56}, {69, 59}, {69, 62}
    }),
    REGULAR_PING_LAYOUT(
            new int[]{62, 64},
            new int[][]{
            {64, 65}, {59, 65}, {64, 62}, {59, 62}, {64, 59}, {59, 59}, {64, 56}, {59, 56}, {69, 50}, {64, 50},
            {69, 47}, {64, 47}, {69, 44}, {64, 44}, {69, 41}, {64, 41}, {59, 41}, {59, 44}, {59, 47}, {59, 50}
    });

    private final int[] startingPoint;

    private final int[][] layout;

    PatchLayout(int[] startingPoint, int[][] layout) {
        this.startingPoint = startingPoint;
        this.layout = layout;
    }

    public WorldPoint getStartingPoint() {
        if (this.startingPoint.length == 0) {
            return null;
        }
        return WorldPoint.fromScene(client, this.startingPoint[0], this.startingPoint[1], 0);
    }
}
