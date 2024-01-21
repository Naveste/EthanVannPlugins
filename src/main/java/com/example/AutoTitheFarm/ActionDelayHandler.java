package com.example.AutoTitheFarm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;

public class ActionDelayHandler {

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private boolean waitForAction;

    @Inject
    private ActionDelayHandler() {
    }
}
