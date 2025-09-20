package com.madu59;

import java.util.List;

import com.madu59.config.SettingsManager;
import com.madu59.mixin.client.ChatHudAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatUtils {

    public static List<ChatHudLine.Visible> getVisibleChatMessages() {
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        return ((ChatHudAccessor) chatHud).getVisibleMessages();
    }

    public static List<ChatHudLine> getChatMessages() {
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        List<ChatHudLine> messages = ((ChatHudAccessor) chatHud).getMessages();
        return messages;
    }

    public static int positionOfItemPickUpMessage(ItemStack itemStack, List<ChatHudLine> messages) {
        return positionOfItemPickUpMessage(itemStack, messages, 200);
    }

    public static int positionOfItemPickUpMessage(ItemStack itemStack, List<ChatHudLine> messages, int maxDelay) {
        int id = 0;

        for(ChatHudLine line: messages){
            if(MinecraftClient.getInstance().inGameHud.getTicks() > line.creationTick() + maxDelay){
                return -1;
            }
            String content = line.content().getString();
            if(content.contains(I18n.translate("flh.picked-up-message")) && content.contains(itemStack.getItemName().getString())){
                return id;
            }
            id++;
        }
        return -1;
    }

    public static Text sendOrEditItemPickUpMessage(ItemStack itemStack){
        List<ChatHudLine> messages = getChatMessages();
        int position = -1;
        String groupingDelaySetting = SettingsManager.ENABLE_PIKCUP_WARNING_GROUPING.getValueAsString();
        if(!groupingDelaySetting.equals("Never")){
            int maxDelay = 200;
            if(groupingDelaySetting.equals("5s")){
                maxDelay = 100;
            }
            else if(groupingDelaySetting.equals("3s")){
                maxDelay = 60;
            }

            position = positionOfItemPickUpMessage(itemStack, messages, maxDelay);
        }
        int count = itemStack.getCount();
        Text name = itemStack.getItemName();

        if(position < 0){
            return sendItemPickUpMessage(name, count);
        }
        else{
            count += extractPickupCount(messages.get(position).content(), itemStack);
            ((ChatHudAccessor) MinecraftClient.getInstance().inGameHud.getChatHud()).getVisibleMessages().remove(position);
            return sendItemPickUpMessage(name, count);
        }
    }

    public static int extractPickupCount(Text text, ItemStack stack) {
        List<Text> siblings = text.getSiblings();
        if (siblings.size() == 3) {
            return Integer.parseInt(siblings.get(0).getString());
        }
        return 0;
    }

    public static Text sendItemPickUpMessage(ItemStack itemStack){
        return sendItemPickUpMessage(itemStack.getItemName(), itemStack.getCount());
    }

    public static Text sendItemPickUpMessage(Text name, int count){
        Text text = Text.translatable("flh.picked-up-message").append(Text.literal(String.valueOf(count))).append(" ").append(name);
        MinecraftClient.getInstance().player.sendMessage(((MutableText) text).styled(style -> style.withColor(Formatting.GRAY)), false);
        return text;
    }
}