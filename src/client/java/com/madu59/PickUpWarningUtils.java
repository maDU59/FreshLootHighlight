package com.madu59;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

import com.madu59.config.SettingsManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PickUpWarningUtils {

    private final static String[] POSSIBLE_PATHS = {
        "textures/item/%s.png",
        "textures/block/%s.png",
        "textures/block/%s_front.png",
        "textures/block/%s_empty.png",
        "textures/block/%s_side.png",
        "textures/block/%s_top.png"
    };
    private final static Map<String, String> CONVERTION_LAYER_MAP  = Map.ofEntries(
        entry("wood", "log"),
        entry("oak", "oak_planks"),
        entry("pale_oak", "pale_oak_planks"),
        entry("spruce", "spruce_planks"),
        entry("birch", "birch_planks"),
        entry("jungle", "jungle_planks"),
        entry("acacia", "acacia_planks"),
        entry("dark_oak", "dark_oak_planks"),

        entry("mangrove", "mangrove_planks"),
        entry("cherry", "cherry_planks"),
        entry("bamboo", "bamboo_planks"),
        entry("crimson", "crimson_planks"),
        entry("warped", "warped_planks"),
        entry("moss", "moss_block")
    );
    private final static Identifier NULL_IDENTIFIER = Identifier.of("null");
    private static final Set<Identifier> dynamicTextures = new HashSet<>();

    public static Text createMessage(ItemStack itemStack){
        return createMessage(itemStack.getItem().getName(), itemStack.getCount(),false);
    }

    public static Text createMessage(Item item, int count){
        return createMessage(item.getName(), count,false);
    }

    public static Text createMessage(Text name, int count){
        return createMessage(name,count,false);
    }

    public static Text createMessage(Text name, int count, boolean forceLong){
        if(forceLong || SettingsManager.PICKUP_WARNING_STYLE.getValueAsString().equals("Long")){
            return Text.translatable("fresh-loot-highlight.picked-up-message").append(Text.literal(String.valueOf(count))).append(" ").append(name);
        }
        else{
            return Text.literal("> ").append(Text.literal(String.valueOf(count))).append(" ").append(name);
        }
    }

    public static List<PickUpWarning> AddOrEditMessage(ItemStack itemStack, List<PickUpWarning> messages){
        int count = itemStack.getCount();
        Text name = itemStack.getItemName();
        Item item = itemStack.getItem();
        if(!SettingsManager.ENABLE_PICKUP_WARNING_GROUPING.getValueAsString().equals("Never")){
            int id = 0;
            int maxDelay = getDelay(SettingsManager.ENABLE_PICKUP_WARNING_GROUPING.getValueAsString());
            for(PickUpWarning warning: messages){
                if(isMessageOfSameItem(warning, itemStack.getItemName()) && MinecraftClient.getInstance().inGameHud.getTicks() < warning.creationTick + maxDelay){
                    count += extractCountFromMessage(warning.message);
                    messages.remove(id);
                    messages.add(new PickUpWarning(item, count));
                    if(Boolean.TRUE.equals(SettingsManager.ENABLE_PICK_UP_WARNING_NARRATOR.getValue())) NarratorUtils.narrate(createMessage(item.getName(), count, true));
                    return messages;
                }
                id++;
            }
        }
        messages.add(new PickUpWarning(item, count));
        if(Boolean.TRUE.equals(SettingsManager.ENABLE_PICK_UP_WARNING_NARRATOR.getValue())) NarratorUtils.narrate(createMessage(item.getName(), count, true));
        return messages;
    }

    public static boolean isMessageOfSameItem(PickUpWarning warning, Text itemName){
        List<Text> siblings = warning.message.getSiblings();
        if (siblings.size() == 3 && siblings.getLast() == itemName) {
            return true;
        }
        return false;
    }

    public static int extractCountFromMessage(Text message){
        List<Text> siblings = message.getSiblings();
        return Integer.parseInt(siblings.get(0).getString());
    }

    public static int getDelay(String setting){
        int maxDelay = 200;
        if(setting.equals("5s")){
            maxDelay = 100;
        }
        else if(setting.equals("3s")){
            maxDelay = 60;
        }
        return maxDelay;
    }

    public static Identifier getItemTexture(Item item){

        Identifier itemId = Registries.ITEM.getId(item);
        Identifier iconId = tryToFind2DTexture(itemId);
        if(iconId != NULL_IDENTIFIER) return iconId;

        String itemType = getItemType(itemId);
        String itemMaterial = getItemMaterial(itemId);
        String convertedItemType = convertionLayer(itemType);
        iconId = tryToFind2DTexture("minecraft", itemMaterial + "_side");
        if(iconId != NULL_IDENTIFIER) return iconId;
        iconId = tryToFind2DTexture("minecraft", itemMaterial + "_top");
        if(iconId != NULL_IDENTIFIER) return iconId;
        iconId = tryToFind2DTexture("minecraft", itemMaterial + "_" + convertedItemType);
        if(iconId != NULL_IDENTIFIER) return iconId;
        itemMaterial = convertionLayer(itemMaterial, convertedItemType);
        iconId = tryToFind2DTexture("minecraft", itemMaterial + "_" + convertedItemType);
        if(iconId != NULL_IDENTIFIER) return iconId;
        Identifier registeredId = Identifier.of(FreshLootHighlight.MOD_ID, "dynamic/" + itemMaterial + "_" + convertedItemType + ".png");
        if(dynamicTextures.contains(registeredId)) return registeredId;
        iconId = generateIconFromMask(itemId, itemMaterial, convertedItemType);
        if(iconId != NULL_IDENTIFIER) return iconId;
        System.out.println("Could not find texture for " + itemId.toString() + " (tried material: " + itemMaterial + ", type: " + itemType + ")");
        return NULL_IDENTIFIER;
    }

    public static String getItemType(Identifier itemId){
        String path = itemId.getPath(); // e.g. "oak_fence"

        String[] parts = path.split("_");
        return parts[parts.length - 1]; // e.g. "fence"
    }

    public static String getItemMaterial(Identifier itemId){
        String path = itemId.getPath(); // e.g. "dark_oak_fence"
        String[] parts = path.split("_");

        if (parts.length <= 1) {
            return path;
        }

        int size = parts.length - 1;
        int start =0;

        if(Arrays.stream(parts).anyMatch("gate"::equals) || Arrays.stream(parts).anyMatch("pressure"::equals)){
            size -= 1;
        }
        if(parts[0].equals("waxed")){
            start = 1;
        }

        return String.join("_", java.util.Arrays.copyOfRange(parts, start, size)); // e.g. dark_oak
    }

    public static String convertionLayer(String str){
        if(CONVERTION_LAYER_MAP.containsKey(str)){
            return CONVERTION_LAYER_MAP.get(str);
        }
        else{
            return str;
        }
    }

    public static String convertionLayer(String str, String type){
        if(CONVERTION_LAYER_MAP.containsKey(str)){
            return CONVERTION_LAYER_MAP.get(str);
        }
        else{
            if(type.equals("carpet") && !str.equals("moss")) return str + "_wool";
            return str;
        }
    }

    public static Identifier generateIconFromMask(Identifier itemId, String material, String type){
        try{
            ResourceManager rm = MinecraftClient.getInstance().getResourceManager();

            // Load the material texture (e.g. minecraft:textures/block/oak_planks.png)
            Identifier materialTexture = Identifier.of(itemId.getNamespace(), "textures/block/" + material + ".png");
            NativeImage icon = NativeImage.read(rm.getResourceOrThrow(materialTexture).getInputStream());

            // Load the mask (e.g. fresh-loot-highlight:textures/mask/fence_mask.png)
            Identifier maskId = Identifier.of(FreshLootHighlight.MOD_ID, "textures/mask/" + type + ".png");
            NativeImage mask = NativeImage.read(rm.getResourceOrThrow(maskId).getInputStream());

            for (int y = 0; y < icon.getHeight(); y++) {
                for (int x = 0; x < icon.getWidth(); x++) {
                    int pixelColor = icon.getColorArgb(x, y);

                    int a = (pixelColor >> 24) & 0xFF;
                    int r = (pixelColor >> 16) & 0xFF;
                    int g = (pixelColor >> 8) & 0xFF;
                    int b = pixelColor & 0xFF;

                    int maskColor = mask.getColorArgb(x, y);

                    float maskAlpha = ((maskColor >> 24) & 0xFF)/255.0f;

                    if(maskAlpha > 0.1) icon.setColor(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                    else icon.setColor(x, y, 0x00000000);
                }
            }

            // Register as a dynamic texture
            Identifier resultId = Identifier.of(FreshLootHighlight.MOD_ID, "dynamic/" + material + "_" + type + ".png");
            dynamicTextures.add(resultId);
            MinecraftClient.getInstance().getTextureManager().registerTexture(resultId, new NativeImageBackedTexture(() -> resultId.toString(),icon));

            return resultId;
        }
        catch(IOException ignored){
            return NULL_IDENTIFIER;
        }
    }

    public static Identifier tryToFind2DTexture(Identifier itemId){
        return tryToFind2DTexture(itemId.getNamespace(), itemId.getPath(), POSSIBLE_PATHS);
    }

    public static Identifier tryToFind2DTexture(String namespace, String path){
        return tryToFind2DTexture(namespace, path, POSSIBLE_PATHS);
    }

    public static Identifier tryToFind2DTexture(String namespace, String path, String[] possiblePaths){
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        for (String pathPattern : possiblePaths) {
            Identifier textureId = Identifier.of(namespace, String.format(pathPattern, path));
            try {
                resourceManager.getResourceOrThrow(textureId);
                return textureId;
            } catch (IOException ignored) {

            }
        }
        return NULL_IDENTIFIER;
    }
}

