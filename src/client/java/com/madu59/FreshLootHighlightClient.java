package com.madu59;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class FreshLootHighlightClient implements ClientModInitializer {

	/*TO-DO:
	 * Pick-up notifications
	 * Only mark as new when never found before toggle
	 * Handle hotbar in creative inventory categories
	 */

	private final static MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static List<Integer> freshSlots = new ArrayList<Integer>();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null) return;
			/*
			while (client.options.swapHandsKey.wasPressed()) {
				
			}
			*/
			PlayerInventory inv = client.player.getInventory();
			if(freshSlots.contains(inv.getSelectedSlot())){
				freshSlots.remove((Integer)inv.getSelectedSlot());
			}
			if(freshSlots.contains(36)){
				freshSlots.remove((Integer)36);
			}

			for (int slotId: freshSlots){
				if(inv.getStack(slotId).isEmpty())
					freshSlots.remove((Integer)slotId);
			}

		});
	}

	static public void onPickUpEvent(ItemStack pickedUpItemStack) {
		if(CLIENT.player == null) return;

		PlayerInventory inv = CLIENT.player.getInventory();
		int count = pickedUpItemStack.getCount();

		System.out.println("Player picked up: " + pickedUpItemStack);

		for(int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if(stack.getItem() == pickedUpItemStack.getItem()) {
				count -= 64 - stack.getCount();
				if(count < 0){
					System.out.println("Item added in an already created stack: " + i);
					return;
				}
			}
		}
		freshSlots.add(inv.getEmptySlot());
		System.out.println("Item added in new stack: " + inv.getEmptySlot());
	}
}