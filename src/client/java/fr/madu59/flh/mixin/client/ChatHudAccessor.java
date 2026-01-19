package fr.madu59.flh.mixin.client;

import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatComponent.class)
public interface ChatHudAccessor {

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getVisibleMessages();

    @Accessor("allMessages")
    List<GuiMessage> getMessages();
}