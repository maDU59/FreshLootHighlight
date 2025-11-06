package fr.madu59.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.FreshLootHighlightClient;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements TickablePacketListener, ClientPlayPacketListener {

    @Shadow
    private ClientWorld world;

    private static final Set<Integer> recentPickups = new HashSet<>();

    @Inject(at = @At("HEAD"), method = "onItemPickupAnimation")
    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int cId = packet.getCollectorEntityId();
        int pId = player.getId();

        if (cId == pId) {
            Entity entity = this.world.getEntityById(packet.getEntityId());
            
            if (!recentPickups.add(packet.getEntityId()) && entity instanceof ItemEntity itemEntity && !itemEntity.isRemoved()) {
                System.out.println("Picked up item: " + itemEntity.getStack().getItem().toString());
                FreshLootHighlightClient.onPickUpEvent(itemEntity.getStack());
            }

            MinecraftClient.getInstance().execute(() -> recentPickups.remove(packet.getEntityId()));
        }
    }
}