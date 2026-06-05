package fr.madu59.flh.config.configscreen;

import fr.madu59.flh.FreshLootHighlight;
import fr.madu59.flh.config.SettingsManager;
import fr.madu59.flh.config.highlights.HighlightsToggle;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FreshLootHighlightConfigScreen extends Screen {
    
    private MyConfigListWidget list;
    private final Screen parent;

    protected FreshLootHighlightConfigScreen(Screen parent) {
        super(Component.literal("Fresh Loot Highlight config"));
        this.parent = parent;
    }

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                literal("flhConfig")
                    .executes(context -> {
                        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new FreshLootHighlightConfigScreen(null)));
                        return 1;
                    })
            );
        });
    }

    @Override
    protected void init() {
        super.init();
        this.list = new MyConfigListWidget(this.minecraft, this.width, this.height - 80, 40, 26);

        list.category("fresh-loot-highlight.config.category_slot_highlighter").build();
        list.button(SettingsManager.ENABLE_SLOT_HIGHLIGHTER).build();
        list.button(SettingsManager.SLOT_HIGHLIGHTER_SPRITE).indent().isEnabled(() -> SettingsManager.ENABLE_SLOT_HIGHLIGHTER.getValue() != HighlightsToggle.NEVER).build();
        list.category("fresh-loot-highlight.config.category_pickup_warning").build();
        list.button(SettingsManager.ENABLE_PICKUP_WARNING).build();
        list.slider(SettingsManager.PICKUP_WARNING_TIMEOUT).indent().range(3f, 10f).step(0.1f).isEnabled(() -> SettingsManager.ENABLE_PICKUP_WARNING.getValue()).build();
        list.slider(SettingsManager.PICKUP_WARNING_GROUPING_TIMEOUT).indent().range(0f, 10f).step(0.1f).isEnabled(() -> SettingsManager.ENABLE_PICKUP_WARNING.getValue()).build();
        list.button(SettingsManager.PICKUP_WARNING_STYLE).indent().isEnabled(() -> SettingsManager.ENABLE_PICKUP_WARNING.getValue()).build();
        list.button(SettingsManager.PICKUP_WARNING_HUD_POSITION).indent().isEnabled(() -> SettingsManager.ENABLE_PICKUP_WARNING.getValue()).build();
        list.button(SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM).indent().isEnabled(() -> SettingsManager.ENABLE_PICKUP_WARNING.getValue()).build();
        list.button(SettingsManager.ENABLE_PICK_UP_WARNING_NARRATOR).indent().isEnabled(() -> SettingsManager.ENABLE_PICKUP_WARNING.getValue()).build();

        Button doneButton = Button.builder(Component.literal("Done"), b -> {
            this.minecraft.setScreen(this.parent);
            SettingsManager.saveSettings(SettingsManager.ALL_OPTIONS);
        }).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build();

        this.addRenderableWidget(this.list);
        this.addRenderableWidget(doneButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        SettingsManager.saveSettings(SettingsManager.ALL_OPTIONS);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.list.extractRenderState(context, mouseX, mouseY, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}