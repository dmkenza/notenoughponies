package com.neponies;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.neponies.DebugUtils.runDebugBreeding;

public class NotEnoughPonies implements ModInitializer {

    public static final String MOD_ID = "notenoughponies";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static final boolean isDebug = Boolean.getBoolean("mymod.debug");

    public record DebugBreedPayload() implements CustomPayload {
        public static final CustomPayload.Id<DebugBreedPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "debug_breed"));
        public static final PacketCodec<RegistryByteBuf, DebugBreedPayload> CODEC = PacketCodec.unit(new DebugBreedPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Override
    public void onInitialize() {
        if (isDebug) {
            PayloadTypeRegistry.playC2S().register(DebugBreedPayload.ID, DebugBreedPayload.CODEC);

            ServerPlayNetworking.registerGlobalReceiver(DebugBreedPayload.ID, (payload, context) -> {
                context.server().execute(() -> {
                    if (context.player().getWorld() instanceof ServerWorld serverWorld) {
                        runDebugBreeding(context.player(), serverWorld);
                    }
                });
            });
        }
    }
}