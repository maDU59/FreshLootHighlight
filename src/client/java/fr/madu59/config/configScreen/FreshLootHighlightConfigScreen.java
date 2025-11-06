package fr.madu59.config.configScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import fr.madu59.config.SettingsManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class FreshLootHighlightConfigScreen extends Screen {
    private MyConfigListWidget list;

    private final String INDENT = " ⤷  ";

    protected FreshLootHighlightConfigScreen(Screen parent) {
        super(Text.literal("Fresh Loot Highlight config"));
        this.parent = parent;
    }

    private final Screen parent;

    @Override
    protected void init() {
        super.init();
        // Create the scrolling list
        this.list = new MyConfigListWidget(this.client, this.width, this.height - 80, 40, 26);

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

        ButtonWidget doneButton = ButtonWidget.builder(Text.literal("Done"), b -> {
            this.client.setScreen(this.parent);
            SettingsManager.saveSettings(SettingsManager.ALL_OPTIONS);
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build();

        this.addDrawableChild(this.list);
        this.addDrawableChild(doneButton);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
        SettingsManager.saveSettings(SettingsManager.ALL_OPTIONS);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.list.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}