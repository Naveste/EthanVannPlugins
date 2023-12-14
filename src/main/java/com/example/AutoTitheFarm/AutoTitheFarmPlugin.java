package com.example.AutoTitheFarm;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.ObjectPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.annotations.VarCStr;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.RandomUtils;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.EthanApiPlugin.Collections.query.TileObjectQuery.getObjectComposition;
import static com.example.PacketUtils.PacketReflection.client;

@Slf4j
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
@PluginDescriptor(name =
        "AutoTitheFarm",
        description = "Will do Tithe Farm for you",
        enabledByDefault = false,
        tags = {""})
public class AutoTitheFarmPlugin extends Plugin {

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AutoTitheFarmConfig config;

    @Inject
    private ClientThread clientThread;

    AutoTitheFarmOverlay overlay;

    @Override
    public void startUp() {
        log.info("Plugin started");
        overlay = new AutoTitheFarmOverlay(client, this, config);
        overlayManager.add(overlay);
        initValues();
    }

    @Override
    public void shutDown() {
        log.info("Plugin shutdown");
        resetValues();
        overlayManager.remove(overlay);
    }

    @Provides
    public AutoTitheFarmConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoTitheFarmConfig.class);
    }

    private static final int EMPTY_PATCH = 27383;

    private static final int WATERING_ANIMATION = 2293;

    private static final int PLANTING_ANIMATION = 2291;

    private static final int DIGGING_ANIMATION = 830;

    private int amountOfPatches;

    public final Set<TileObject> emptyPatches = new LinkedHashSet<>();

    private final List<TileObject> firstPhaseObjectsToFocus = new ArrayList<>();

    private final List<TileObject> secondPhaseObjectsToFocus = new ArrayList<>();

    private final List<TileObject> thirdPhaseObjectsToFocus = new ArrayList<>();

    private final List<TileObject> fourthPhaseObjectsToFocus = new ArrayList<>();

    public boolean waitForAction;

    private boolean isHarvestingPhase;

    private boolean needToRestoreRunEnergy;

    public static int farmingLevel;

    private int[][] patchLayout;

    private int waterChargesCountUsed;

    private int randomCount;

    private boolean foundBlightedPlant;

    private int lastActionTimer;

    private void initValues() {
        farmingLevel = getGetPlayerFarmingLevel();
        patchLayout = config.patchLayout().getLayout();
        randomCount = getRandomCount();
        clientThread.invoke(() -> Inventory.search().withId(ItemID.GRICOLLERS_CAN).first().ifPresent(itm -> InventoryInteraction.useItem(itm, "Check")));
    }

    private void resetValues() {
        emptyPatches.clear();
        firstPhaseObjectsToFocus.clear();
        secondPhaseObjectsToFocus.clear();
        thirdPhaseObjectsToFocus.clear();
        fourthPhaseObjectsToFocus.clear();
        waitForAction = false;
        randomCount = 0;
        needToRestoreRunEnergy = false;
    }

    private boolean gotRequiredItems() {
        Optional<Widget> seedDibber = Inventory.search().withId(ItemID.SEED_DIBBER).first();
        Optional<Widget> spade = Inventory.search().withId(ItemID.SPADE).first();
        // generalized and crude checks for now.
        return getWateringCan() != null && seedDibber.isPresent() && spade.isPresent() && getSeed() != null;
    }

    private int getRandomCount() {
        return RandomUtils.nextInt(2, 9);
    }

    private Widget getWateringCan() {
        Widget wateringCan = Inventory.search().withId(ItemID.GRICOLLERS_CAN).first().orElse(null);
        return wateringCan;
    }

    private Widget getSeed() {
        return Inventory.search().nameContains("seed").first().orElse(null);
    }

    private boolean isNeedToRefillWateringCan() {
        return randomCount == waterChargesCountUsed || waterChargesCountUsed > randomCount;
    }

    private boolean startingNewRun() {
        return emptyPatches.size() == amountOfPatches;
    }

    private int getGetPlayerFarmingLevel() {
        return client.getRealSkillLevel(Skill.FARMING);
    }

    private WorldPoint playerDirection() {
        WorldPoint worldPoint = null;
        int playerOrientation = client.getLocalPlayer().getCurrentOrientation();
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

        switch (playerOrientation) {
            case 151: worldPoint = playerLocation.dx(-1).dy(-2); break;
            case 360: worldPoint = playerLocation.dx(-2).dy(-1); break;
            case 663: worldPoint = playerLocation.dx(-2).dy(1); break;
            case 1385: worldPoint = playerLocation.dx(2).dy(1); break;
            case 1688: worldPoint = playerLocation.dx(2).dy(-1); break;
            default: //
        }

        return worldPoint;
    }

    private int amountOfGrowingPatchesLeft() {
        return TileObjects.search().withAction("Water").result().size();
    }

    private boolean isInsideTitheFarm() {
        if (client.isInInstancedRegion()) {
            return true;
        }
        resetValues();
        return false;
    }

    private void doAction(List<TileObject> collection) {
        TileObject patch = collection.get(0);
        //log.info("Wait for action: " + waitForAction);
        if (waitForAction) {
            return;
        }
        TileObjectInteraction.interact(patch, "Water", "Harvest");
        waitForAction = true;
    }

    private void useItemOnObject(Widget widget, TileObject tileObject) {
        //log.info("Wait for action: " + waitForAction);
        Optional<Widget> optionalWidget = Optional.of(widget);
        if (waitForAction) {
            return;
        }
        optionalWidget.ifPresent(itm -> ObjectPackets.queueWidgetOnTileObject(itm, tileObject));
        waitForAction = true;
    }

    private void captureEmptyPatches() {
        amountOfPatches = patchLayout.length;

        if (!emptyPatches.isEmpty()) {
            emptyPatches.clear();
        }
        for (int[] point : patchLayout) {
            WorldPoint worldPoint = WorldPoint.fromScene(client, point[0], point[1], 0);
            TileObjects.search().withId(EMPTY_PATCH).atLocation(worldPoint).first().ifPresent(emptyPatches::add);
        }
    }

    private void openHerbBox() {
        Inventory.search().withId(ItemID.HERB_BOX).first().ifPresent(itm -> InventoryInteraction.useItem(itm, "Bank-all"));
    }

    private void getLastActionTimer() {
        if (waitForAction) {
            lastActionTimer++;
        } else {
            lastActionTimer = 0;
        }
    }

    private void handleShit() {
        Optional<TileObject> waterBarrel = TileObjects.search().nameContains("Water Barrel").atLocation(WorldPoint.fromLocal(client, 7360, 6720, 0)).first();
        int runEnergy = client.getEnergy() / 100;

        // if 20 ticks have passed and no actions have been made within time limit then something went horribly wrong.
        if (lastActionTimer > (startingNewRun() ? 2 : 20) && !EthanApiPlugin.isMoving()) {
            waitForAction = false;
        }

        if (runEnergy == 100) {
            needToRestoreRunEnergy = false;
        }

        if (startingNewRun()) {
            isHarvestingPhase = false;
            foundBlightedPlant = false;

            if (isNeedToRefillWateringCan()) {
                log.info("Need to refill can");
                useItemOnObject(getWateringCan(), waterBarrel.orElse(null));
                return;
            }

            if (runEnergy < config.minRunEnergyToIdleUnder()) {
                needToRestoreRunEnergy = true;
            }

            if (needToRestoreRunEnergy) {
                return;
            }
        }

        if (!firstPhaseObjectsToFocus.isEmpty()) {
            doAction(firstPhaseObjectsToFocus);
            return;
        }

        List<TileObject> convertedListPatches = new ArrayList<>(emptyPatches);
        if (!emptyPatches.isEmpty() && !isHarvestingPhase && !foundBlightedPlant) {
            useItemOnObject(getSeed(), convertedListPatches.get(0));
            log.info("Planting");
            return;
        }

        List<List<TileObject>> phases = List.of(secondPhaseObjectsToFocus, thirdPhaseObjectsToFocus, fourthPhaseObjectsToFocus);
        for (List<TileObject> phase : phases) {
            if (phase.isEmpty()) {
                continue;
            }
            doAction(phase);
            if (phase != phases.get(phases.size() - 1)) {
                return;
            }
        }
    }

    private void dePopulateList(List<TileObject> list) {
        list.removeIf(tileObject -> playerDirection() != null && playerDirection().equals(tileObject.getWorldLocation()));
    }

    private void handleLobby() {
        Plants neededPlant = null;
        int firstChatOptionId = 0;
        Optional<Widget> firstChatWindowId = Widgets.search().withTextContains("kind of crop").hiddenState(false).first();
        Optional<Widget> secondChatWindowId = Widgets.search().withTextContains("How many seeds").hiddenState(false).first();

        for (Plants plant : Plants.values()) {
            if (plant.getPlant() == null) {
                continue;
            }
            neededPlant = plant.getPlant();
        }

        switch (Objects.requireNonNull(neededPlant)) {
            case GOLOVANOVA: firstChatOptionId = 1; break;
            case BOLOGANO: firstChatOptionId = 2; break;
            case LOGAVANO: firstChatOptionId = 3; break;
        }

        log.info(String.valueOf(neededPlant));
        log.info(String.valueOf(firstChatOptionId));

        if (getSeed() == null) {
            if (secondChatWindowId.isPresent()) {
                client.setVarcStrValue(VarClientStr.INPUT_TEXT, Integer.toString(10000));

                KeyEvent keyPress = new KeyEvent(client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER);
                client.getCanvas().dispatchEvent(keyPress);
                return;
            }
            if (firstChatWindowId.isEmpty()) {
                TileObjects.search().withId(ObjectID.SEED_TABLE).first().ifPresent(obj -> TileObjectInteraction.interact(obj, "Search"));
                return;
            }
            WidgetPackets.queueResumePause(14352385, firstChatOptionId);
            return;
        }
        if (waitForAction) {
            return;
        }
        TileObjects.search().nameContains("Farm door").first().ifPresent(obj -> TileObjectInteraction.interact(obj, "Open"));
        waitForAction = true;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        openHerbBox();
        getLastActionTimer();

        if (!isInsideTitheFarm()) {
            handleLobby();
            return;
        }

        if (!gotRequiredItems()) {
            return;
        }

        captureEmptyPatches();

        if (client.getLocalPlayer().getAnimation() == WATERING_ANIMATION || client.getLocalPlayer().getAnimation() == DIGGING_ANIMATION) {
            dePopulateList(firstPhaseObjectsToFocus);
            dePopulateList(secondPhaseObjectsToFocus);
            dePopulateList(thirdPhaseObjectsToFocus);
            dePopulateList(fourthPhaseObjectsToFocus);
        }

        handleShit();

//        log.info("needToRefillWaterCan: " + isNeedToRefillWateringCan());
//        log.info("Random count: " + randomCount);
//        log.info("waterChargesCountUsed: " + waterChargesCountUsed);
//        log.info("needToRestoreRunEnergy: " + needToRestoreRunEnergy);
        log.info("starting new run: " + startingNewRun());
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        Actor actor = event.getActor();
        int animationId = actor.getAnimation();

        if (!(actor instanceof Player)) {
            return;
        }

        if (animationId == WATERING_ANIMATION
                || animationId == PLANTING_ANIMATION
                || animationId == -1
                || animationId == DIGGING_ANIMATION) {
            waitForAction = false;
        }
    }

    private void populateList(List<TileObject> list, TileObject tileObject) {
        if (!list.contains(tileObject)) {
            list.add(tileObject);
        }
    }

    private void removeObjectFromListIfBlighted(List<TileObject> list, GameObject blightedObject) {
        list.removeIf(tileObject -> blightedObject.getWorldLocation().equals(tileObject.getWorldLocation()));
    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();
        int objectId = gameObject.getId();

        if (gameObject.getWorldLocation().equals(playerDirection()) || startingNewRun()) {
            waitForAction = false;
        }

        for (Plants plant : Plants.values()) {

            if (objectId == plant.getFourthStageId() && amountOfGrowingPatchesLeft() == 0) {
                isHarvestingPhase = true;
            }

            if (objectId == plant.getFirstStageId()) {
                populateList(firstPhaseObjectsToFocus, gameObject);
            } else if (objectId == plant.getSecondStageId()) {
                populateList(secondPhaseObjectsToFocus, gameObject);
            } else if (objectId == plant.getThirdStageId()) {
                populateList(thirdPhaseObjectsToFocus, gameObject);
            } else if (objectId == plant.getFourthStageId()) {
                populateList(fourthPhaseObjectsToFocus, gameObject);
            }
        }

        String objectName = getObjectComposition(gameObject).getName();
        List<List<TileObject>> lists = List.of(firstPhaseObjectsToFocus, secondPhaseObjectsToFocus, thirdPhaseObjectsToFocus, fourthPhaseObjectsToFocus);
        if (!objectName.contains("Blighted")) {
            return;
        }

        foundBlightedPlant = true;
        waitForAction = false;

        for (List<TileObject> list : lists) {
            removeObjectFromListIfBlighted(list, gameObject);
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject gameObject = event.getGameObject();
    }

    private int getGricollersCanCount(String message) {
        Matcher matcher = Pattern.compile("\\d+").matcher(message);
        int intValue = matcher.find() ? (Integer.parseInt(matcher.group()) / 10) : -1;

        switch (intValue) {
            case 1: return 9;
            case 2: return 8;
            case 3: return 7;
            case 4: return 6;
            case 5: return 5;
            case 6: return 4;
            case 7: return 3;
            case 8: return 2;
            case 9: return 1;
            case 10: return 0;
            default: return -1;
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        String message = event.getMessage();

        if (message.contains("Watering can charges") && !isNeedToRefillWateringCan()) {
            waterChargesCountUsed = getGricollersCanCount(message);
        }

        if (message.contains("You fill the watering can")) {
            randomCount = getRandomCount();
            waterChargesCountUsed = 0;
        }

        if (message.contains("can is already full")) {
            randomCount = getRandomCount();
        }
    }

}