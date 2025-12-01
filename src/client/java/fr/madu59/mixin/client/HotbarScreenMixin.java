package fr.madu59.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fr.madu59.FreshLootHighlightClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public abstract class HotbarScreenMixin {

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void renderHotbarItem(GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int id, CallbackInfo Ci) {
        if(FreshLootHighlightClient.freshSlots.contains(id-1)) {
            // Display exlamation mark
            Identifier exclamationMarkTexture = Identifier.fromNamespaceAndPath("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted.png");
            Identifier exclamationMarkTextureAlt = Identifier.fromNamespaceAndPath("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted_alt.png");
            Item item = Minecraft.getInstance().player.getInventory().getItem(id-1).getItem();
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            boolean isFoundForTheFirstTime = FreshLootHighlightClient.foundForTheFirstTime.contains(itemId);
            context.blit(RenderPipelines.GUI_TEXTURED, isFoundForTheFirstTime? exclamationMarkTextureAlt: exclamationMarkTexture, x, y + 2, 0, 0, 14, 14, 14, 14);
        }
    }
}