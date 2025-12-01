package fr.madu59;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import fr.madu59.config.SettingsManager;

public class PickUpWarningUtils {

    public static Component createMessage(ItemStack itemStack){
        return createMessage(itemStack.getItem().getName(), itemStack.getCount(),false);
    }

    public static Component createMessage(Item item, int count){
        return createMessage(item.getName(), count,false);
    }

    public static Component createMessage(Component name, int count){
        return createMessage(name,count,false);
    }

    public static Component createMessage(Component name, int count, boolean forceLong){
        if(forceLong || SettingsManager.PICKUP_WARNING_STYLE.getValueAsString().equals("Long")){
            return Component.translatable("fresh-loot-highlight.picked-up-message").append(Component.literal(String.valueOf(count))).append(" ").append(name);
        }
        else{
            return Component.literal("> ").append(Component.literal(String.valueOf(count))).append(" ").append(name);
        }
    }

    public static List<PickUpWarning> AddOrEditMessage(ItemStack itemStack, List<PickUpWarning> messages){
        int count = itemStack.getCount();
        Component name = itemStack.getItemName();
        Item item = itemStack.getItem();
        if(!SettingsManager.ENABLE_PICKUP_WARNING_GROUPING.getValueAsString().equals("Never")){
            int id = 0;
            int maxDelay = getDelay(SettingsManager.ENABLE_PICKUP_WARNING_GROUPING.getValueAsString());
            for(PickUpWarning warning: messages){
                if(isMessageOfSameItem(warning, name) && Minecraft.getInstance().gui.getGuiTicks() < warning.creationTick + maxDelay){
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

    public static boolean isMessageOfSameItem(PickUpWarning warning, Component itemName){
        List<Component> siblings = warning.message.getSiblings();
        if (siblings.size() == 3 && siblings.getLast() == itemName) {
            return true;
        }
        return false;
    }

    public static int extractCountFromMessage(Component message){
        List<Component> siblings = message.getSiblings();
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

