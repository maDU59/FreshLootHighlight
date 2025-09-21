package com.madu59;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.madu59.config.SettingsManager;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class FreshLootHighlightClient implements ClientModInitializer {

	/*TO-DO:
	 * Make Custom HUD customizable (item icon)
	 * Only mark as new when never found before toggle
	 */

	private final static MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static List<Integer> freshSlots = new ArrayList<Integer>();
	public static List<PickUpWarning> pickUpMessages = new ArrayList<PickUpWarning>();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(FreshLootHighlight.MOD_ID, "pick_up_warning_hud"), FreshLootHighlightClient::render);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null) return;
			PlayerInventory inv = client.player.getInventory();
			if(inv == null) return;
			if(freshSlots.contains(inv.getSelectedSlot())){
				freshSlots.remove((Integer)inv.getSelectedSlot());
			}
			if(freshSlots.contains(36)){
				freshSlots.remove((Integer)36);
			}

			Iterator<Integer> slotsIterator = freshSlots.iterator();
			while (slotsIterator.hasNext()) {
				int slotId = slotsIterator.next();
				if (inv.getStack(slotId).isEmpty() || slotId == -1) {
					slotsIterator.remove();
				}
			}

			Iterator<PickUpWarning> messagesIterator = pickUpMessages.iterator();
			int delay = PickUpWarningUtils.getDelay(SettingsManager.PICKUP_WARNING_TIMEOUT.getValueAsString());
			while (messagesIterator.hasNext()) {
				if (CLIENT.inGameHud.getTicks() > delay + messagesIterator.next().creationTick) {
					messagesIterator.remove();
				}
			}
		});
	}

	static public void onPickUpEvent(ItemStack pickedUpItemStack) {
		if(CLIENT.player == null) return;

		PlayerInventory inv = CLIENT.player.getInventory();
		int count = pickedUpItemStack.getCount();
		Item item = pickedUpItemStack.getItem();

		if(Boolean.TRUE.equals(SettingsManager.ENABLE_PICKUP_WARNING.getValue())){
			pickUpMessages = PickUpWarningUtils.AddOrEditMessage(pickedUpItemStack, pickUpMessages);
		}

		System.out.println("Player picked up: " + pickedUpItemStack);

		for(int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if(stack.getItem() == pickedUpItemStack.getItem()) {
				count -= stack.getMaxCount() - stack.getCount();
				if(count < 0){
					System.out.println("Item added in an already created stack: " + i);
					return;
				}
			}
		}
		freshSlots.add(inv.getEmptySlot());
		System.out.println("Item added in new stack: " + inv.getEmptySlot());
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter){
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int entryX = 0;
		int entryY = 0;
		int tileSizeY = 11;
		boolean isAlignedLeft = false;
		String position = SettingsManager.PICKUP_WARNING_HUD_POSITION.getValueAsString();
		if(position.equals("BOTTOM_RIGHT")){
			entryY = context.getScaledWindowHeight() - tileSizeY * pickUpMessages.size();
		}
		if(position.equals("BOTTOM_RIGHT") || position.equals("TOP_RIGHT")){
			isAlignedLeft = true;
		}

		for(PickUpWarning pickUpWarning: pickUpMessages){
			int entryWidth = textRenderer.getWidth(pickUpWarning.message);
			entryX = isAlignedLeft? context.getScaledWindowWidth() - entryWidth : 0;
			context.fill(entryX, entryY, entryX + entryWidth, entryY  + tileSizeY, 0x99000000);
			context.drawText(textRenderer, pickUpWarning.message, entryX, entryY + (tileSizeY - textRenderer.fontHeight)/2 + 1, 0xFFFFFFFF, false);
			entryY += tileSizeY;
		}
	}
}