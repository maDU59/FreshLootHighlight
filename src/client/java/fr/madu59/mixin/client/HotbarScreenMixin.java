package fr.madu59.mixin.client;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

import fr.madu59.FreshLootHighlightClient;

@Mixin(InGameHud.class)
public abstract class HotbarScreenMixin {

    @Inject(method = "renderHotbarItem", at = @At("TAIL"))
    private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int id, CallbackInfo Ci) {
        if(FreshLootHighlightClient.freshSlots.contains(id-1)) {
            // Display exlamation mark
            Identifier exclamationMarkTexture = Identifier.of("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted.png");
            Identifier exclamationMarkTextureAlt = Identifier.of("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted_alt.png");
            Item item = MinecraftClient.getInstance().player.getInventory().getStack(id-1).getItem();
            Identifier itemId = Registries.ITEM.getId(item);
            boolean isFoundForTheFirstTime = FreshLootHighlightClient.foundForTheFirstTime.contains(itemId);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, isFoundForTheFirstTime? exclamationMarkTextureAlt: exclamationMarkTexture, x, y + 2, 0, 0, 14, 14, 14, 14);
        }
    }
}