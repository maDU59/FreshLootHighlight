package fr.madu59;

import java.util.List;

import fr.madu59.config.SettingsManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class PickUpWarningUtils {

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
                if(isMessageOfSameItem(warning, name) && MinecraftClient.getInstance().inGameHud.getTicks() < warning.creationTick + maxDelay){
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
}

