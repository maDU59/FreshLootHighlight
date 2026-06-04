package fr.madu59.flh.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.flh.FreshLootHighlightClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void renderHotbarItem(GuiGraphicsExtractor context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int id, CallbackInfo Ci) {
        if(FreshLootHighlightClient.freshSlots.contains(id-1)) {
            Item item = Minecraft.getInstance().player.getInventory().getItem(id-1).getItem();
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            boolean isFoundForTheFirstTime = FreshLootHighlightClient.foundForTheFirstTime.contains(itemId);
            FreshLootHighlightClient.highlighterSprite.draw(context, x, y, isFoundForTheFirstTime);
        }
    }
}