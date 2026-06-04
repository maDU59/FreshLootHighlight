package fr.madu59.flh.highlights.sprites;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DefaultSprite {
    
    private static ResourceLocation sprite;

    public static void draw(GuiGraphics context, int x, int y) {
        draw(context, x, y, false);
    }

    public static void draw(GuiGraphics context, int x, int y, boolean isFoundForTheFirstTime) {
        context.blit(sprite, x, y + 2, 0, 0, 14, 14, 14, 14);
    }

}
