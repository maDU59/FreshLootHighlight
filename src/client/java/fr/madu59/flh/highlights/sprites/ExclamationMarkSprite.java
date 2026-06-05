package fr.madu59.flh.highlights.sprites;

import net.minecraft.resources.Identifier;

public class ExclamationMarkSprite extends AbstractSprite {

    private static final Identifier texture = Identifier.fromNamespaceAndPath("fresh-loot-highlight", "textures/gui/sprites/highlight_exclamation_mark.png");

    public ExclamationMarkSprite() {
        super(texture);
    }
}