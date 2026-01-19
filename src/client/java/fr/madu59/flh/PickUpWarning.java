package fr.madu59.flh;

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
}
