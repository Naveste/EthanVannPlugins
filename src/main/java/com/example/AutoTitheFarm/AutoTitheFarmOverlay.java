package com.example.AutoTitheFarm;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.api.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class AutoTitheFarmOverlay extends Overlay {
    AutoTitheFarmPlugin plugin;
    Client client;

    AutoTitheFarmOverlay(Client client, AutoTitheFarmPlugin plugin) {
        this.plugin = plugin;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGHEST);

    }

    private void renderTextLocation(Graphics2D graphics, String text, WorldPoint worldPoint, Color color) {
        LocalPoint point = LocalPoint.fromWorld(client, worldPoint);
        if (point == null) {
            return;
        }
        Point textLocation = Perspective.getCanvasTextLocation(client, graphics, point, text, 0);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
        }
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setFont(FontManager.getRunescapeFont());

        List<TileObject> patches = new ArrayList<>(plugin.emptyPatches);
        for (TileObject tileObject : patches) {
            renderTextLocation(graphics, String.valueOf(patches.indexOf(tileObject) + 1), tileObject.getWorldLocation(), Color.WHITE);
        }

        return null;
    }
}