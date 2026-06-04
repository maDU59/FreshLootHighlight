package fr.madu59.flh.highlights.sprites;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public abstract class AbstractSprite {
    
    private Identifier sprite;
    private int color = ARGB.color(255, 255, 0);
    private int colorAlt = ARGB.color(255, 0, 255);

    protected AbstractSprite(Identifier sprite) {
        this.sprite = sprite;
    }

    public void draw(GuiGraphicsExtractor context, int x, int y) {
        draw(context, x, y, false);
    }

    public void draw(GuiGraphicsExtractor context, int x, int y, boolean isFoundForTheFirstTime) {
        context.blit(RenderPipelines.GUI_TEXTURED, sprite, x, y, 0, 0, 16, 16, 16, 16, isFoundForTheFirstTime ? colorAlt : color);
    }
}
