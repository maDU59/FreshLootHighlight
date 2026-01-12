package fr.madu59;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix3x2fStack;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.madu59.config.ClientCommands;
import fr.madu59.config.Option;
import fr.madu59.config.SettingsManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

public class FreshLootHighlightClient implements ClientModInitializer {

	/*TO-DO:
	 * Test for previous versions compatibility
	 */

	private static final Minecraft CLIENT = Minecraft.getInstance();
	public static List<Integer> freshSlots = new ArrayList<Integer>();
	public static List<PickUpWarning> pickUpMessages = new ArrayList<PickUpWarning>();
	public static String serverId = "NoWorldOrServer";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static List<Identifier> alreadyFound = new ArrayList<Identifier>();
	public static List<Identifier> foundForTheFirstTime = new ArrayList<Identifier>();

	@Override
	public void onInitializeClient() {
		ClientCommands.register();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(FreshLootHighlight.MOD_ID, "pick_up_warning_hud"), FreshLootHighlightClient::render);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// This runs when the client enters a world

			if (CLIENT.getSingleplayerServer() == null) {
				// Multiplayer
				ServerData info = CLIENT.getCurrentServer();
				serverId = info != null ? info.ip.replace(":", "_") : "unknown_server";
			} else {
				// Singleplayer
				serverId = CLIENT.getSingleplayerServer().getWorldPath(LevelResource.ROOT)
					.getParent().getFileName().toString();
			}
			loadAlreadyFound();
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null) return;
			Inventory inv = client.player.getInventory();
			if(inv == null) return;
			if(freshSlots.contains(inv.getSelectedSlot())){
				freshSlots.remove(Integer.valueOf(inv.getSelectedSlot()));
				FreshLootHighlightClient.foundForTheFirstTime.remove(BuiltInRegistries.ITEM.getKey(inv.getSelectedItem().getItem()));
			}
			if(freshSlots.contains(36)){
				freshSlots.remove((Integer)36);
			}
			inv.getContainerSize();

			Iterator<Integer> slotsIterator = freshSlots.iterator();
			while (slotsIterator.hasNext()) {
				int slotId = slotsIterator.next();
				if (slotId >= inv.getContainerSize() || slotId < 0 || inv.getItem(slotId).isEmpty()) {
					slotsIterator.remove();
				}
			}

			Iterator<PickUpWarning> messagesIterator = pickUpMessages.iterator();
			float delay = SettingsManager.PICKUP_WARNING_TIMEOUT.getValue() * 20;
			while (messagesIterator.hasNext()) {
				if (CLIENT.gui.getGuiTicks() > delay + messagesIterator.next().creationTick) {
					messagesIterator.remove();
				}
			}
		});
	}

	static public void onPickUpEvent(ItemStack pickedUpItemStack) {
		if(CLIENT.player == null) return;

		Inventory inv = CLIENT.player.getInventory();
		int count = pickedUpItemStack.getCount();
		Item item = pickedUpItemStack.getItem();

		if(SettingsManager.ENABLE_PICKUP_WARNING.getValue()){
			pickUpMessages = PickUpWarningUtils.AddOrEditMessage(pickedUpItemStack, pickUpMessages);
		}

		for(int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if(stack.getItem() == pickedUpItemStack.getItem()) {
				count -= stack.getMaxStackSize() - stack.getCount();
				if(count < 0){
					return;
				}
			}
		}

		Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
		if(alreadyFound.contains(itemId) && SettingsManager.ENABLE_SLOT_HIGHLIGHTER.getValue() == Option.SlotHighlighterToggle.ALWAYS){
			freshSlots.add(inv.getFreeSlot());
		}
		else if(SettingsManager.ENABLE_SLOT_HIGHLIGHTER.getValue() != Option.SlotHighlighterToggle.NEVER){
			alreadyFound.add(itemId);
			freshSlots.add(inv.getFreeSlot());
			foundForTheFirstTime.add(itemId);
			saveAlreadyFound();
		}
	}

	private static void render(GuiGraphics context, DeltaTracker tickCounter){
		Font textRenderer = Minecraft.getInstance().font;
		int entryX = 0;
		int entryY = 0;
		int tileSizeY = 11;
		boolean isAlignedLeft = false;
		Option.WarningPosition position = SettingsManager.PICKUP_WARNING_HUD_POSITION.getValue();
		if(position == Option.WarningPosition.BOTTOM_RIGHT){
			entryY = context.guiHeight() - tileSizeY * pickUpMessages.size();
		}
		if(position == Option.WarningPosition.BOTTOM_RIGHT || position == Option.WarningPosition.TOP_RIGHT){
			isAlignedLeft = true;
		}

		for(PickUpWarning pickUpWarning: pickUpMessages){
			boolean showItem = SettingsManager.PICKUP_WARNING_HUD_SHOW_ITEM.getValue();
			int entryWidth = textRenderer.width(pickUpWarning.message) + (showItem? 11: 0);
			entryX = isAlignedLeft? context.guiWidth() - entryWidth : 0;
			context.fill(entryX, entryY, entryX + entryWidth, entryY  + tileSizeY, 0x99000000);
			Matrix3x2fStack matrices = context.pose();
			if(showItem){
				matrices.scale(0.5F);
				context.renderFakeItem(pickUpWarning.itemStack, (int)((entryX + 1.5) * 2), (int)((entryY + 1.5) * 2));
				matrices.scale(2F);
			}
			context.drawString(textRenderer, pickUpWarning.message, entryX + (showItem? 11: 0), entryY + (tileSizeY - textRenderer.lineHeight)/2 + 1, 0xFFFFFFFF, false);
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
          	alreadyFound = new ArrayList<>(raw.stream().map(Identifier::parse).toList());
        }
		catch(Exception e) {
			System.out.println(e);
		}
    }
}