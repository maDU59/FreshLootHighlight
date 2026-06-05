package fr.madu59.flh;

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

import fr.madu59.flh.config.Option.WarningPosition;
import fr.madu59.flh.config.SettingsManager;
import fr.madu59.flh.config.configscreen.FreshLootHighlightConfigScreen;
import fr.madu59.flh.config.highlights.HighlightsToggle;
import fr.madu59.flh.highlights.sprites.AbstractSprite;
import fr.madu59.flh.highlights.sprites.ExclamationMarkSprite;
import fr.madu59.flh.warnings.PickUpWarning;
import fr.madu59.flh.warnings.PickUpWarningUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

public class FreshLootHighlightClient implements ClientModInitializer {

	public static List<Integer> freshSlots = new ArrayList<Integer>();
	public static List<PickUpWarning> pickUpMessages = new ArrayList<PickUpWarning>();
	public static String serverId = "NoWorldOrServer";
	private static final Gson GSON = new GsonBuilder().create();
	private static List<Identifier> alreadyFound = new ArrayList<Identifier>();
	public static List<Identifier> foundForTheFirstTime = new ArrayList<Identifier>();

	@Override
	public void onInitializeClient() {
		FreshLootHighlightConfigScreen.registerCommand();

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(FreshLootHighlight.MOD_ID, "pick_up_warning_hud"), FreshLootHighlightClient::render);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {

			if (Minecraft.getInstance().getSingleplayerServer() == null) {
				ServerData info = Minecraft.getInstance().getCurrentServer();
				serverId = info != null ? info.ip.replace(":", "_") : "unknown_server";
			} else {
				serverId = Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT)
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
			if(freshSlots.contains(Inventory.SLOT_OFFHAND)){
				freshSlots.remove((Integer)Inventory.SLOT_OFFHAND);
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
				if (Minecraft.getInstance().gui.getGuiTicks() > delay + messagesIterator.next().creationTick) {
					messagesIterator.remove();
				}
			}
		});
	}

	static public void onPickUpEvent(ItemStack pickedUpItemStack) {
		if(Minecraft.getInstance().player == null) return;

		Inventory inv = Minecraft.getInstance().player.getInventory();
		int count = pickedUpItemStack.getCount();
		Item item = pickedUpItemStack.getItem();

		if(SettingsManager.ENABLE_PICKUP_WARNING.getValue()){
			pickUpMessages = PickUpWarningUtils.addOrEditWarning(pickedUpItemStack, pickUpMessages);
		}

		for(int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if(stack.getItem() == item) {
				count -= stack.getMaxStackSize() - stack.getCount();
				if(count < 0){
					return;
				}
			}
		}

		Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
		if(SettingsManager.ENABLE_SLOT_HIGHLIGHTER.getValue() == HighlightsToggle.ALWAYS){
			freshSlots.add(inv.getFreeSlot());
		}
		else if(SettingsManager.ENABLE_SLOT_HIGHLIGHTER.getValue() != HighlightsToggle.NEVER){
			if(!alreadyFound.contains(itemId)){
				foundForTheFirstTime.add(itemId);
				alreadyFound.add(itemId);
				saveAlreadyFound();
			}
			freshSlots.add(inv.getFreeSlot());
		}
	}

	private static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter){
		int entryX = 0;
		int entryY = 0;
		int tileSizeY = 11;
		boolean isAlignedLeft = false;
		WarningPosition position = SettingsManager.PICKUP_WARNING_HUD_POSITION.getValue();
		if(position == WarningPosition.BOTTOM_RIGHT){
			entryY = context.guiHeight() - tileSizeY * pickUpMessages.size();
		}
		if(position == WarningPosition.BOTTOM_RIGHT || position == WarningPosition.TOP_RIGHT){
			isAlignedLeft = true;
		}

		for(PickUpWarning pickUpWarning: pickUpMessages){
			entryX = isAlignedLeft? context.guiWidth() - pickUpWarning.getWidth() : 0;
			pickUpWarning.draw(context, entryX, entryY, tileSizeY);
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