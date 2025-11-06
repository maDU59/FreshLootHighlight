package fr.madu59;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;

public class NarratorUtils {

    private static final NarratorManager narratorManager = MinecraftClient.getInstance().getNarratorManager();

    public static void narrate(Text message) {
        narratorManager.narrate(message);
    }

    public static void narrate(String message) {
        narratorManager.narrate(Text.literal(message));
    }
}
