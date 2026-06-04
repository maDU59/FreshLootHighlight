package fr.madu59.flh.warnings;

import org.joml.Matrix3x2fStack;

import fr.madu59.flh.config.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PickUpWarning {

    public Component message;
    public ItemStack itemStack;
    public int count;
    public Item item;
    public int creationTick;
    private Font textRenderer = Minecraft.getInstance().font;

    public PickUpWarning(ItemStack itemStack){
        this.itemStack = itemStack;
        this.count = itemStack.getCount();
        this.item = itemStack.getItem();
        this.message = PickUpWarningUtils.createMessage(this.item, this.count);
        this.creationTick = Minecraft.getInstance().gui.getGuiTicks();
    }

    public PickUpWarning(Item item, int count){
        this.itemStack = item.getDefaultInstance();
        this.count = count;
        this.item = item;
        this.message = PickUpWarningUtils.createMessage(this.item, this.count);
        this.creationTick = Minecraft.getInstance().gui.getGuiTicks();
    }

    public boolean isSameItem(PickUpWarning otherWarning){
        return isSameItem(this, otherWarning);
    }

    public static boolean isSameItem(PickUpWarning warning, PickUpWarning otherWarning){
        return isSameItem(warning.itemStack, otherWarning.itemStack);
    }

    public static boolean isSameItem(PickUpWarning warning, ItemStack itemStack){
        return isSameItem(warning.itemStack, itemStack);
    }

    private static boolean isSameItem(ItemStack itemStack, ItemStack otherItemStack){
        return ItemStack.isSameItem(itemStack, otherItemStack);
    }

    private boolean showItem(){
        return SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM.getValue();
    }

    public int getWidth(){
        return textRenderer.width(this.message) + (showItem()? 11: 0);
    }

    public void draw(GuiGraphicsExtractor context, int x, int y, int height){
        boolean showItem = SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM.getValue();
        int entryWidth = textRenderer.width(this.message) + (showItem()? 11: 0);
        context.fill(x, y, x + entryWidth, y  + height, 0x99000000);
        Matrix3x2fStack matrices = context.pose();
        if(showItem){
            matrices.scale(0.5F);
            context.fakeItem(this.itemStack, (int)((x + 1.5) * 2), (int)((y + 1.5) * 2));
            matrices.scale(2F);
        }
        context.text(textRenderer, this.message, x + (showItem()? 11: 0), y + (height - textRenderer.lineHeight)/2 + 1, 0xFFFFFFFF, false);
    }
}
