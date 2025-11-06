package fr.madu59.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

import fr.madu59.FreshLootHighlightClient;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
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

            Identifier exclamationMarkTexture = Identifier.of("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted.png");
            Identifier exclamationMarkTextureAlt = Identifier.of("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted_alt.png");


            if(FreshLootHighlightClient.freshSlots.contains(inventoryIndex)) {

                Item item = MinecraftClient.getInstance().player.getInventory().getStack(inventoryIndex).getItem();
                Identifier itemId = Registries.ITEM.getId(item);
                boolean isFoundForTheFirstTime = FreshLootHighlightClient.foundForTheFirstTime.contains(itemId);

                // Slot screen coordinates
                int x = slot.x + this.x;
                int y = slot.y + this.y;

                // Display exlamation mark
                context.drawTexture(RenderPipelines.GUI_TEXTURED, isFoundForTheFirstTime? exclamationMarkTextureAlt: exclamationMarkTexture, x, y + 2, 0, 0, 14, 14, 14, 14);

                // Delete from fresh list on hovering
                if(mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    FreshLootHighlightClient.freshSlots.remove((Integer)inventoryIndex);
                    if(isFoundForTheFirstTime){
                        FreshLootHighlightClient.foundForTheFirstTime.remove(itemId);
                    }
                }
            }
        }
    }
}