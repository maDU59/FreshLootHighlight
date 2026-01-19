package fr.madu59.flh.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.flh.FreshLootHighlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    public CreativeInventoryScreenMixin(InventoryMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void drawNewItemBadges(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;

        //Only display in inventory tab
        if(screen.isInventoryOpen()){
            for(int i = 0; i < screen.getMenu().slots.size(); i++) {
                Slot slot = screen.getMenu().slots.get(i);

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

                Identifier exclamationMarkTexture = Identifier.fromNamespaceAndPath("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted.png");
                Identifier exclamationMarkTextureAlt = Identifier.fromNamespaceAndPath("fresh-loot-highlight", "textures/gui/sprites/warning_highlighted_alt.png");

                if(FreshLootHighlightClient.freshSlots.contains(inventoryIndex)) {

                    Item item = Minecraft.getInstance().player.getInventory().getItem(inventoryIndex).getItem();
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                    boolean isFoundForTheFirstTime = FreshLootHighlightClient.foundForTheFirstTime.contains(itemId);

                    // Slot screen coordinates
                    int x = slot.x + this.leftPos;
                    int y = slot.y + this.topPos;

                    // Display exlamation mark
                    context.blit(RenderPipelines.GUI_TEXTURED, isFoundForTheFirstTime? exclamationMarkTextureAlt: exclamationMarkTexture, x, y + 2, 0, 0, 14, 14, 14, 14);

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
        else{
            //Handle hotbar inside of categories
        }
    }
}