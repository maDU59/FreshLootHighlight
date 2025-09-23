package com.madu59.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.madu59.FreshLootHighlight;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsManager {

    public static List<Option> ALL_OPTIONS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(FreshLootHighlight.MOD_ID + ".json");

    public static Option ENABLE_PICKUP_WARNING = loadOptionWithDefaults(
        "ENABLE_PICKUP_WARNING",
        "fresh-loot-highlight.config.enable_pickup_warning",
        "fresh-loot-highlight.config.enable_pickup_warning_desc",
        true,
        true,
        List.of(true,false)
    );

    public static Option ENABLE_PICKUP_WARNING_GROUPING = loadOptionWithDefaults(
        "ENABLE_PICKUP_WARNING_GROUPING",
        "fresh-loot-highlight.config.enable_pickup_warning_grouping",
        "fresh-loot-highlight.config.enable_pickup_warning_grouping_desc",
        "10s",
        "10s",
        List.of("10s","5s","3s","Never")
    );

    public static Option PICKUP_WARNING_TIMEOUT = loadOptionWithDefaults(
        "PICKUP_WARNING_TIMEOUT",
        "fresh-loot-highlight.config.pickup_warning_timeout",
        "fresh-loot-highlight.config.pickup_warning_timeout_desc",
        "10s",
        "10s",
        List.of("10s","5s","3s")
    );

    public static Option PICKUP_WARNING_STYLE = loadOptionWithDefaults(
        "PICKUP_WARNING_HUD_STYLE",
        "fresh-loot-highlight.config.pickup_warning_hud_style",
        "fresh-loot-highlight.config.pickup_warning_hud_style_desc",
        "Default",
        "Default",
        List.of("Default","Long")
    );

    public static Option PICKUP_WARNING_HUD_POSITION = loadOptionWithDefaults(
        "PICKUP_WARNING_HUD_POSITION",
        "fresh-loot-highlight.config.pickup_warning_hud_position",
        "fresh-loot-highlight.config.pickup_warning_hud_position_desc",
        "TOP_LEFT",
        "TOP_LEFT",
        List.of("TOP_LEFT","TOP_RIGHT","BOTTOM_RIGHT")
    );

    public static Option PICKUP_WARNING_HUD_SHOW_ITEM = loadOptionWithDefaults(
        "PICKUP_WARNING_HUD_SHOW_ITEM",
        "fresh-loot-highlight.config.pickup_warning_hud_show_item",
        "fresh-loot-highlight.config.pickup_warning_hud_show_item_desc",
        true,
        true,
        List.of(true, false)
    );

    public static Option ENABLE_SLOT_HIGHLIGHTER = loadOptionWithDefaults(
        "ENABLE_SLOT_HIGHLIGHTER",
        "fresh-loot-highlight.config.enable_slot_highlighter",
        "fresh-loot-highlight.config.enable_slot_highlighter_desc",
        true,
        true,
        List.of(true, "Only if never seen before", false)
    );

    public static Option ENABLE_PICK_UP_WARNING_NARRATOR = loadOptionWithDefaults(
        "ENABLE_PICK_UP_WARNING_NARRATOR",
        "fresh-loot-highlight.config.enable_pick_up_warning_narrator",
        "fresh-loot-highlight.config.enable_pick_up_warning_narrator_desc",
        true,
        true,
        List.of(true, false)
    );

    public static List<String> getAllOptionsId(){
        List<String> list = new ArrayList<>();
        for (Option option : ALL_OPTIONS){
            list.add(option.getId());
            }
        return list;
    }

    public static boolean setOptionValue(String optionId, Object value){
        for (Option option : ALL_OPTIONS){
            System.out.println(optionId + ": " + option.getId() + ", " + option.getId().equalsIgnoreCase(optionId));
            System.out.println(value + ": " + option.getPossibleValues() + ", " + option.getPossibleValues().contains(value));
            if(option.getId().equalsIgnoreCase(optionId)){
                int index = option.getPossibleValues().stream().map(Object::toString).collect(Collectors.toList()).indexOf((String) value);
                if (option.getPossibleValues().contains(value)){
                    option.setValue(value);
                    return true;
                }
                else if(index != -1){
                    option.setValue(option.getPossibleValues().get(index));
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getOptionPossibleValues(String optionId){
        for (Option option : ALL_OPTIONS){
            if (option.getId().equalsIgnoreCase(optionId)){
                return option.getPossibleValues().stream().map(Object::toString).collect(Collectors.toList());
            }
        }
        return null;
    }

    public static int getRGBColorFromSetting(String colorName) {
        int[] colors = getColorFromSetting(colorName);
        return colors[2] + colors[1] * 256 + colors[0] * 256 * 256 + 255 * 256 * 256 *256;
    }

    public static float[] convertColorToFloat(int[] colors){
        float red = colors[0]/(float)255.0;
        float green = colors[1]/(float)255.0;
        float blue = colors[2]/(float)255.0;
        return new float[] {red, green, blue};
    }

    public static float convertAlphaToFloat(int alpha){
        float alphaFloat = alpha/(float)255.0;
        return alphaFloat;
    }

    public static int[] getColorFromSetting(String colorName) {
        int red = 0, green = 0, blue = 0;
        switch (colorName) {
            case "Red":
                red = 255;
                break;
            case "Green":
                green = 255;
                break;
            case "Blue":
                blue = 255;
                break;
            case "Yellow":
                red = 255;
                green = 255;
                break;
            case "Cyan":
                green = 255;
                blue = 255;
                break;
            case "Magenta":
                red = 255;
                blue = 255;
                break;
            case "Purple":
                red = 128;
                green = 0;
                blue = 128;
                break;
            case "White":
                red = 255;
                green = 255;
                blue = 255;
                break;
            case "Grey":
                red = 128;
                green = 128;
                blue = 128;
                break;
            case "Black":
                red = 0;
                green = 0;
                blue = 0;
                break;
            default:
                red = 255; // Default to red if unknown
        }

        return new int[] {red, green, blue};
    }

    public static void saveSettings(List<Option> options) {
        Map<String, Option> map = toMap(options);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(map, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Option> toMap(List<Option> options) {
        Map<String, Option> map = new LinkedHashMap<>();
        for (Option option : options) {
            map.put(option.getId(), option);
        }
        return map;
    }

    private static Option loadOption(String key) {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Type type = new TypeToken<Map<String, Option>>() {}.getType();
            Map<String, Option> map = GSON.fromJson(reader, type);
            return map.get(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Option loadOptionWithDefaults(String id, String name, String description, Object value, Object defaultValue, List<Object> possibleValues) {
        Option loadedOption = loadOption(id);
        System.out.println("Loaded option for " + id + ": " + (loadedOption == null ? "null" : loadedOption.getValueAsString()));
        if (loadedOption == null) {
            return new Option(
                    id,
                    name,
                    description,
                    value,
                    defaultValue,
                    possibleValues
            );
        } else {
            loadedOption.setPossibleValues(possibleValues);
            loadedOption.setName(name);
            loadedOption.setDescription(description);
            SettingsManager.ALL_OPTIONS.add(loadedOption);
            return loadedOption;
        }
    }
    
}
