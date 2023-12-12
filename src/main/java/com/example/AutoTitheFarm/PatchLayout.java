package com.example.AutoTitheFarm;


import lombok.Getter;

@Getter
public enum PatchLayout {
    LOW_PING_LAYOUT(new int[][]{
            {64, 65}, {59, 65}, {64, 62}, {59, 62}, {64, 59}, {59, 59}, {64, 56}, {59, 56}, {59, 50}, {64, 50},
            {59, 47}, {64, 47}, {59, 44}, {64, 44}, {59, 41}, {64, 41}, {69, 41}, {69, 44}, {69, 47}, {69, 50},
            {69, 56}, {69, 59}, {69, 62}
    });

    private final int[][] layout;

    PatchLayout(int[][] layout) {
        this.layout = layout;
    }
}
