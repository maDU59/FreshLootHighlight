package fr.madu59.flh.highlights.sprites;

import net.minecraft.resources.Identifier;

public class SmallSprite extends AbstractSprite {

    private static final Identifier texture = Identifier.fromNamespaceAndPath("fresh-loot-highlight", "textures/gui/sprites/highlight_small.png");

    public SmallSprite() {
        super(texture);
    }
}