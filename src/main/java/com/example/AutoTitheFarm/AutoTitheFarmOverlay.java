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

//        for (WorldPoint point : plugin.area) {
//            if (point != null) {
//                renderTile(graphics, LocalPoint.fromWorld(client, point), new Color(55, 214, 238, 0), 1, new Color(24, 141, 213, 47));
//            }
//        }

        List<TileObject> patches = new ArrayList<>(plugin.emptyPatches);
        for (TileObject tileObject : patches) {
            renderTextLocation(graphics, String.valueOf(patches.indexOf(tileObject) + 1), tileObject.getWorldLocation(), Color.WHITE);
        }

//        if (plugin.playerDirection() != null) {
//            renderTile(graphics, LocalPoint.fromWorld(client, plugin.playerDirection()),
//                    new Color(55, 214, 238, 0), 1, new Color(255, 0, 0, 76));
//        }

        return null;
    }

    private void renderArea(final Graphics2D graphics, final LocalPoint dest, final Color color,
                            final double borderWidth, final Color fillColor) {
        if (dest == null) {
            return;
        }

        final Polygon poly = Perspective.getCanvasTileAreaPoly(client, dest, 3);

        if (poly == null) {
            return;
        }
        OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final double borderWidth, final Color fillColor) {
        if (dest == null) {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

        if (poly == null) {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
    }
}