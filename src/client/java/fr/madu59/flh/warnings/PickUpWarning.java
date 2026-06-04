package fr.madu59.flh.warnings;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PickUpWarning {

    public Component message;
    public ItemStack itemStack;
    public int count;
    public Item item;
    public int creationTick;

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
}
