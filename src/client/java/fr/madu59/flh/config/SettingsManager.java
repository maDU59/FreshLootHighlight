package fr.madu59.flh.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.madu59.flh.FreshLootHighlight;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsManager {

    public static List<Option<?>> ALL_OPTIONS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(FreshLootHighlight.MOD_ID + ".json");
    private static Map<String, String> loadedSettings = loadSettings();

    public static Option<Boolean> ENABLE_PICKUP_WARNING = loadOptionWithDefaults(
        "ENABLE_PICKUP_WARNING",
        "fresh-loot-highlight.config.enable_pickup_warning",
        "fresh-loot-highlight.config.enable_pickup_warning_desc",
        true
    );

    public static Option<Float> PICKUP_WARNING_GROUPING_TIMEOUT = loadOptionWithDefaults(
        "ENABLE_PICKUP_WARNING_GROUPING",
        "fresh-loot-highlight.config.enable_pickup_warning_grouping",
        "fresh-loot-highlight.config.enable_pickup_warning_grouping_desc",
        8f
    );

    public static Option<Float> PICKUP_WARNING_TIMEOUT = loadOptionWithDefaults(
        "PICKUP_WARNING_TIMEOUT",
        "fresh-loot-highlight.config.pickup_warning_timeout",
        "fresh-loot-highlight.config.pickup_warning_timeout_desc",
        8f
    );

    public static Option<Option.WarningStyle> PICKUP_WARNING_STYLE = loadOptionWithDefaults(
        "PICKUP_WARNING_HUD_STYLE",
        "fresh-loot-highlight.config.pickup_warning_hud_style",
        "fresh-loot-highlight.config.pickup_warning_hud_style_desc",
        Option.WarningStyle.DEFAULT
    );

    public static Option<Option.WarningPosition> PICKUP_WARNING_HUD_POSITION = loadOptionWithDefaults(
        "PICKUP_WARNING_HUD_POSITION",
        "fresh-loot-highlight.config.pickup_warning_hud_position",
        "fresh-loot-highlight.config.pickup_warning_hud_position_desc",
        Option.WarningPosition.TOP_LEFT
    );

    public static Option<Boolean> PICKUP_WARNING_HUD_SHOW_ITEM = loadOptionWithDefaults(
        "PICKUP_WARNING_HUD_SHOW_ITEM",
        "fresh-loot-highlight.config.pickup_warning_hud_show_item",
        "fresh-loot-highlight.config.pickup_warning_hud_show_item_desc",
        true
    );

    public static Option<Option.SlotHighlighterToggle> ENABLE_SLOT_HIGHLIGHTER = loadOptionWithDefaults(
        "ENABLE_SLOT_HIGHLIGHTER",
        "fresh-loot-highlight.config.enable_slot_highlighter",
        "fresh-loot-highlight.config.enable_slot_highlighter_desc",
        Option.SlotHighlighterToggle.ALWAYS
    );

    public static Option<Boolean> ENABLE_PICK_UP_WARNING_NARRATOR = loadOptionWithDefaults(
        "ENABLE_PICK_UP_WARNING_NARRATOR",
        "fresh-loot-highlight.config.enable_pick_up_warning_narrator",
        "fresh-loot-highlight.config.enable_pick_up_warning_narrator_desc",
        true
    );

    public static List<String> getAllOptionsId(){
        List<String> list = new ArrayList<>();
        for (Option<?> option : ALL_OPTIONS){
            list.add(option.getId());
            }
        return list;
    }

    public static <T> boolean setOptionValue(String optionId, String value){
        for (Option<?> option : ALL_OPTIONS){
            if(option.getId().equalsIgnoreCase(optionId)){
                if (option.value instanceof Float){
                    try{
                        Float floatVal = Float.parseFloat(value);
                        setOptionValueHelper(option, floatVal);
                        return true;
                    }
                    catch(Exception e){ 
                        return false;
                    }
                }
                else if (option.value instanceof Enum<?> en){
                    try{
                        Enum<?> enumValue = Enum.valueOf(en.getDeclaringClass(), value);
                        setOptionValueHelper(option, enumValue);
                        return true;
                    }
                    catch(Exception e){ 
                        return false;
                    }
                }
                else if (option.value instanceof Boolean){
                    Boolean boolValue = Boolean.valueOf(value);
                    setOptionValueHelper(option, boolValue);
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> void setOptionValueHelper(Option<T> option, Object value) {
        option.setValue((T) value);
    }

    public static <T> List<String> getOptionPossibleValues(String optionId){
        for (Option<?> option : ALL_OPTIONS){
            if (option.getId().equalsIgnoreCase(optionId)){
                return option.getPossibleValues().stream().map(Object::toString).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
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

    public static void saveSettings(List<Option<?>> options) {
        Map<String, String> map = toMap(options);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(map, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> toMap(List<Option<?>> options) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Option<?> option : options) {
            map.put(option.getId(), option.value.toString());
        }
        return map;
    }

    private static Map<String, String> loadSettings() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> map = GSON.fromJson(reader, type);
            return map;
        } catch (Exception e) {
            FreshLootHighlight.LOGGER.info("[FreshLootHighlight] Config file not found or invalid, using default");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOptionValue(String key, T defaultValue) {
        if (loadedSettings == null || !loadedSettings.containsKey(key)) return null;
        else if (defaultValue instanceof Enum<?> e){
            return (T) Enum.valueOf(e.getDeclaringClass(), loadedSettings.get(key));
        }
        else if (defaultValue instanceof Float){
            return (T) Float.valueOf(loadedSettings.get(key));
        }
        else return null;
    }

    private static <T> Option<T> loadOptionWithDefaults(String id, String name, String description, T defaultValue) {
        T optionValue= getOptionValue(id, defaultValue);
        if (optionValue == null) optionValue = defaultValue;
        Option<T> option = new Option<T>(
                id,
                name,
                description,
                optionValue,
                defaultValue
        );
        return option;
    }
}
