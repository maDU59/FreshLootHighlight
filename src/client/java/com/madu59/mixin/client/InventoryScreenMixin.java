package com.madu59.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;

import com.madu59.FreshLootHighlightClient;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void drawNewItemBadges(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;

        for(int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
            Slot slot = screen.getScreenHandler().slots.get(i);

            int inventoryIndex;
			if(i >= 9 && i <= 35) {
				inventoryIndex = i; // main inventory
			} else if(i >= 36 && i <= 44) {
				inventoryIndex = i - 36; // hotbar maps to 0–8
			} else if(i >= 5 && i <= 8) {
				inventoryIndex = i + 32; // armor: boots(5)->0, leggings(6)->1, chest(7)->2, helmet(8)->3
			} else if(i == 45) {
				inventoryIndex = 36; // offhand
			} else {
				inventoryIndex = -1; // crafting, skip
			}

            if(FreshLootHighlightClient.freshSlots.contains(inventoryIndex)) {

                System.out.println("Slot " + i + " is a fresh slot");

                // Coordonnées du slot
                int x = slot.x + this.x;
                int y = slot.y + this.y;

                // Petit rectangle rouge
                context.fill(x, y + 12, x + 4, y + 16, 0xFFFF0000);

                // Supprime le badge si la souris survole le slot
                if(mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    FreshLootHighlightClient.freshSlots.remove((Integer)inventoryIndex);
                    System.out.println("Removed slot " + i + " from fresh slots");
                }
            }
        }
    }
}