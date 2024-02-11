package com.example.AutoScurriusPrayer;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.PrayerInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import static com.example.PacketUtils.PacketReflection.client;

@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
@PluginDescriptor(name =
        "AutoScurriusPrayer",
        enabledByDefault = false,
        tags = {""})
public class AutoScurriusPrayerPlugin extends Plugin {

    @Inject
    private AutoScurriusPrayerConfig config;

    @Provides
    public AutoScurriusPrayerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoScurriusPrayerConfig.class);
    }

    private Prayer toPray;

    private NPC getBoss() {
        return NPCs.search().withName("Scurrius").walkable().alive().first().orElse(null);
    }

    private NPC giantRat() {
        return NPCs.search().withName("Giant rat").walkable().alive().nearestToPlayer().orElse(null);
    }

    private boolean isPlayerInsideBossRoom() {
        return client.isInInstancedRegion();
    }

    private void disablePrayer() {
        for (Prayer prayer : Prayer.values()) {
            if (client.isPrayerActive(prayer)) {
                PrayerInteraction.setPrayerState(prayer, false);
            }
        }
    }

    private void handlePrayer() {
        if (getBoss() == null) {
            toPray = Prayer.PROTECT_FROM_MELEE;
        }

        if (fightActive()) {
            PrayerInteraction.flickPrayers(toPray != null ? toPray : Prayer.PROTECT_FROM_MELEE, config.offensivePrayer().getPrayer());
        } else {
            disablePrayer();
        }
    }

    private boolean fightActive() {
        return !NPCs.search().filter(npc -> npc != null && npc == getBoss() || npc == giantRat()).walkable().alive().result().isEmpty();
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!isPlayerInsideBossRoom()) {
            disablePrayer();
            return;
        }
        handlePrayer();
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event) {
        final Projectile projectile = event.getProjectile();
        final int MAGIC_ATTACK = 2640;
        final int RANGED_ATTACK = 2642;
        boolean isMagicAttack = projectile.getId() == MAGIC_ATTACK;
        boolean isRangedAttack = projectile.getId() == RANGED_ATTACK;
//        int fullCycleDuration = projectile.getEndCycle() - projectile.getStartCycle();

        if (!(isMagicAttack || isRangedAttack)) {
            return;
        }

        if (projectile.getRemainingCycles() >= 35 && projectile.getRemainingCycles() <= 50) {
            if (isMagicAttack) {
                toPray = Prayer.PROTECT_FROM_MAGIC;
            }
            if (isRangedAttack) {
                toPray = Prayer.PROTECT_FROM_MISSILES;
            }
        } else if (projectile.getRemainingCycles() <= 15) {
            toPray = Prayer.PROTECT_FROM_MELEE;
        }
    }
}
