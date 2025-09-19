package com.madu59.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.madu59.FreshLootHighlightClient;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements TickablePacketListener, ClientPlayPacketListener {

    @Shadow
    private ClientWorld world;

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(at = @At("HEAD"), method = "onItemPickupAnimation")
    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        // This method will be executed at the head of injected method.
        // Invoke ahead here do no harm, according to this method's implementation.
        NetworkThreadUtils.forceMainThread(packet, this, this.client);

        ClientPlayerEntity player = this.client.player;
        if (player == null) return;
        int cId = packet.getCollectorEntityId();
        int pId = player.getId();
        // Is this item picked by "me" or other players?
        if (cId == pId) {
            Entity entity = this.world.getEntityById(packet.getEntityId());
            // This item might be an ExperienceOrbEntity and we don't want to speak this sort of thing.
            if (entity instanceof ItemEntity itemEntity) {
                FreshLootHighlightClient.onPickUpEvent(itemEntity.getStack());
            }
        }
    }
}