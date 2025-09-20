package com.madu59.mixin.client;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

import com.madu59.FreshLootHighlightClient;

@Mixin(InGameHud.class)
public abstract class HotbarScreenMixin {

    @Inject(method = "renderHotbarItem", at = @At("TAIL"))
    private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int id, CallbackInfo Ci) {
        if(FreshLootHighlightClient.freshSlots.contains(id-1)) {
            // Display exlamation mark
            Identifier exclamationMarkTexture = Identifier.of("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted.png");
            context.drawTexture(RenderPipelines.GUI_TEXTURED, exclamationMarkTexture, x, y + 2, 0, 0, 14, 14, 14, 14);
        }
    }
}