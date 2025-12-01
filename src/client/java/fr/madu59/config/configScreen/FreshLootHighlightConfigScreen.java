package fr.madu59.config.configScreen;

import fr.madu59.config.SettingsManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FreshLootHighlightConfigScreen extends Screen {
    private MyConfigListWidget list;

    private final String INDENT = " ⤷  ";

    protected FreshLootHighlightConfigScreen(Screen parent) {
        super(Component.literal("Fresh Loot Highlight config"));
        this.parent = parent;
    }

    private final Screen parent;

    @Override
    protected void init() {
        super.init();
        // Create the scrolling list
        this.list = new MyConfigListWidget(this.minecraft, this.width, this.height - 80, 40, 26);

        // Example: Add categories + buttons
        list.addCategory("fresh-loot-highlight.config.category_slot_highlighter");
        list.addButton(SettingsManager.ENABLE_SLOT_HIGHLIGHTER, btn -> {
            SettingsManager.ENABLE_SLOT_HIGHLIGHTER.setToNextValue();
        });
        list.addCategory("fresh-loot-highlight.config.category_pickup_warning");
        list.addButton(SettingsManager.ENABLE_PICKUP_WARNING, btn -> {
            SettingsManager.ENABLE_PICKUP_WARNING.setToNextValue();
        });
        list.addButton(SettingsManager.ENABLE_PICKUP_WARNING_GROUPING, btn -> {
            SettingsManager.ENABLE_PICKUP_WARNING_GROUPING.setToNextValue();
        }, INDENT);
        list.addButton(SettingsManager.PICKUP_WARNING_TIMEOUT, btn -> {
            SettingsManager.PICKUP_WARNING_TIMEOUT.setToNextValue();
        }, INDENT);
        list.addButton(SettingsManager.PICKUP_WARNING_STYLE, btn -> {
            SettingsManager.PICKUP_WARNING_STYLE.setToNextValue();
        }, INDENT);
        list.addButton(SettingsManager.PICKUP_WARNING_HUD_POSITION, btn -> {
            SettingsManager.PICKUP_WARNING_HUD_POSITION.setToNextValue();
        }, INDENT);
        list.addButton(SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM, btn -> {
            SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM.setToNextValue();
        }, INDENT);
        list.addButton(SettingsManager.ENABLE_PICK_UP_WARNING_NARRATOR, btn -> {
            SettingsManager.ENABLE_PICK_UP_WARNING_NARRATOR.setToNextValue();
        }, INDENT);

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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.list.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}