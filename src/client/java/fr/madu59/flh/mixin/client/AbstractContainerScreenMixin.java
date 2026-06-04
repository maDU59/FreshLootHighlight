package fr.madu59.flh.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.flh.FreshLootHighlightClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin{

    @Shadow
    Slot hoveredSlot;
    
    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void extractSlot(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if(slot != null && slot.container instanceof Inventory && slot.hasItem()) {
            int index = slot.getContainerSlot();
            if(FreshLootHighlightClient.freshSlots.contains(index)) {
                int x = slot.x;
                int y = slot.y;

                FreshLootHighlightClient.highlighterSprite.draw(context, x, y, FreshLootHighlightClient.foundForTheFirstTime.contains(BuiltInRegistries.ITEM.getKey(slot.getItem().getItem())));

                if(hoveredSlot != null && hoveredSlot.getContainerSlot() == index) {
                    FreshLootHighlightClient.freshSlots.remove((Integer)index);
                }
            }
        }
    }
}
