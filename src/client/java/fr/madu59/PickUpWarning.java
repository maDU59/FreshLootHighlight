package fr.madu59;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PickUpWarning {

    public Text message;
    public ItemStack itemStack;
    public int count;
    public Item item;
    public int creationTick;
    public Identifier textureId = null;

    public PickUpWarning(ItemStack itemStack){
        this.itemStack = itemStack;
        this.count = itemStack.getCount();
        this.item = itemStack.getItem();
        this.message = PickUpWarningUtils.createMessage(this.item, this.count);
        this.creationTick = MinecraftClient.getInstance().inGameHud.getTicks();
    }

    public PickUpWarning(Item item, int count){
        this.itemStack = null;
        this.count = count;
        this.item = item;
        this.message = PickUpWarningUtils.createMessage(this.item, this.count);
        this.creationTick = MinecraftClient.getInstance().inGameHud.getTicks();
    }

    public void setTexture(Identifier textureId){
        this.textureId = textureId;
    }

}
