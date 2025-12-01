package fr.madu59;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class NarratorUtils {

    private static final GameNarrator narratorManager = Minecraft.getInstance().getNarrator();

    public static void narrate(Component message) {
        narratorManager.saySystemChatQueued(message);
    }

    public static void narrate(String message) {
        narratorManager.saySystemChatQueued(Component.literal(message));
    }
}
