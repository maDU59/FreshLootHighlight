package fr.madu59.flh.config.highlights;

import java.util.function.Supplier;

import fr.madu59.flh.highlights.sprites.AbstractSprite;
import fr.madu59.flh.highlights.sprites.ExclamationMarkSprite;
import fr.madu59.flh.highlights.sprites.SmallSprite;

public enum HighlightsSprite {
    EXCLAMATION_MARK(ExclamationMarkSprite::new),
    SMALL(SmallSprite::new);

    private final Supplier<? extends AbstractSprite> factory;
    private AbstractSprite instance;

    HighlightsSprite(Supplier<? extends AbstractSprite> factory) {
        this.factory = factory;
    }

    public AbstractSprite getSprite() {
        if (this.instance == null) {
            this.instance = this.factory.get(); 
        }
        return this.instance;
    }
}
