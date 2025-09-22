package com.madu59;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.RenderTickCounter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.madu59.config.SettingsManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

public class FreshLootHighlightClient implements ClientModInitializer {

	/*TO-DO:
	 * Add new masks for fence gates, fence
	 * Accessibility: tell what item is picked up using narrator
	 */

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static List<Integer> freshSlots = new ArrayList<Integer>();
	public static List<PickUpWarning> pickUpMessages = new ArrayList<PickUpWarning>();
	public static String serverId = "NoWorldOrServer";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static List<Identifier> alreadyFound = new ArrayList<Identifier>();
	public static List<Identifier> foundForTheFirstTime = new ArrayList<Identifier>();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(FreshLootHighlight.MOD_ID, "pick_up_warning_hud"), FreshLootHighlightClient::render);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// This runs when the client enters a world

			if (CLIENT.getServer() == null) {
				// Multiplayer
				ServerInfo info = CLIENT.getCurrentServerEntry();
				serverId = info != null ? info.address.replace(":", "_") : "unknown_server";
			} else {
				// Singleplayer
				serverId = CLIENT.getServer().getSavePath(WorldSavePath.ROOT)
					.getParent().getFileName().toString();
			}
			loadAlreadyFound();
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null) return;
			PlayerInventory inv = client.player.getInventory();
			if(inv == null) return;
			if(freshSlots.contains(inv.getSelectedSlot())){
				freshSlots.remove((Integer)inv.getSelectedSlot());
				FreshLootHighlightClient.foundForTheFirstTime.remove(Registries.ITEM.getId(inv.getSelectedStack().getItem()));
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

		for(int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if(stack.getItem() == pickedUpItemStack.getItem()) {
				count -= stack.getMaxCount() - stack.getCount();
				if(count < 0){
					return;
				}
			}
		}

		Identifier itemId = Registries.ITEM.getId(item);
		if(alreadyFound.contains(itemId)){
			freshSlots.add(inv.getEmptySlot());
		}
		else{
			alreadyFound.add(itemId);
			freshSlots.add(inv.getEmptySlot());
			foundForTheFirstTime.add(itemId);
			saveAlreadyFound();
		}
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
			Identifier textureId = pickUpWarning.textureId;
			boolean showItem = (boolean) SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM.getValue();
			if(showItem && textureId == null){
				textureId = PickUpWarningUtils.getItemTexture(pickUpWarning.item);
			}
			entryX = isAlignedLeft? context.getScaledWindowWidth() - entryWidth : 0;
			context.fill(entryX, entryY, entryX + entryWidth + (showItem? 11: 0), entryY  + tileSizeY, 0x99000000);
			if(showItem) context.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, entryX + 2, entryY + 2, 0, 0, 7, 7, 7, 7);
			context.drawText(textRenderer, pickUpWarning.message, entryX + (showItem? 11: 0), entryY + (tileSizeY - textRenderer.fontHeight)/2 + 1, 0xFFFFFFFF, false);
			entryY += tileSizeY;
		}
	}

	public static void saveAlreadyFound() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(serverId).resolve(FreshLootHighlight.MOD_ID + ".json");
		try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(alreadyFound.stream().map(Identifier::toString).toList(), writer);
            }
			catch (IOException e) {
				e.printStackTrace();
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAlreadyFound() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(serverId).resolve(FreshLootHighlight.MOD_ID + ".json");
        try (Reader reader = Files.newBufferedReader(path)) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> raw = GSON.fromJson(reader, listType);
          	alreadyFound = new ArrayList<>(raw.stream().map(Identifier::of).toList());
        }
		catch(Exception e) {
			System.out.println(e);
		}
    }
}