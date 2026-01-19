package fr.madu59.flh.config.configScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<FreshLootHighlightConfigScreen> getModConfigScreenFactory() {
        return FreshLootHighlightConfigScreen::new;
    }
}
