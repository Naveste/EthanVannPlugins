package com.example.AutoScurriusPrayer;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Prayer;

@Getter(AccessLevel.PACKAGE)
public enum OffensivePrayer {
    PIETY(Prayer.PIETY),
    AUGURY(Prayer.AUGURY),
    RIGOUR(Prayer.RIGOUR),
    CHIVALRY(Prayer.CHIVALRY),
    EAGLE_EYE(Prayer.EAGLE_EYE),
    MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT);

    private final Prayer prayer;

    OffensivePrayer(Prayer prayer) {
        this.prayer = prayer;
    }
}
