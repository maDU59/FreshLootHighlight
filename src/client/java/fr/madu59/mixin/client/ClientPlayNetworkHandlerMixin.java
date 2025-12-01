package fr.madu59.mixin.client;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.FreshLootHighlightClient;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin implements TickablePacketListener, ClientGamePacketListener {

    @Shadow
    private ClientLevel level;

    private static final Set<Integer> recentPickups = new HashSet<>();

    @Inject(at = @At("HEAD"), method = "handleTakeItemEntity")
    public void onItemPickupAnimation(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int cId = packet.getPlayerId();
        int pId = player.getId();

        if (cId == pId) {
            Entity entity = this.level.getEntity(packet.getItemId());
            
            if (!recentPickups.add(packet.getItemId()) && entity instanceof ItemEntity itemEntity && !itemEntity.isRemoved()) {
                System.out.println("Picked up item: " + itemEntity.getItem().getItem().toString());
                FreshLootHighlightClient.onPickUpEvent(itemEntity.getItem());
            }

            Minecraft.getInstance().execute(() -> recentPickups.remove(packet.getItemId()));
        }
    }
}