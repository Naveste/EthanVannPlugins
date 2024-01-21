package com.example.AutoTitheFarm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
public class ActionDelayHandler {

    private boolean waitForAction;


    private int lastActionTimer;

    @Inject
    private ActionDelayHandler() {
    }

    public void handleLastActionTimer() {
        if (this.isWaitForAction()) {
            this.lastActionTimer++;
        } else {
            this.lastActionTimer = 0;
        }
    }
}
