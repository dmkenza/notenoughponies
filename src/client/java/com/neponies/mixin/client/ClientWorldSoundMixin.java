package com.neponies.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.neponies.NEPoniesConfig.isPonyVillagerAmbientSoundsEnabled;
import static com.neponies.NEPoniesConfig.isPonyVillagerInOriginalModEnabled;
import static com.neponies.NEPoniesConfig.isPonyVillagerSoundsEnabled;
import static com.neponies.VillagerSounds.getVillagerSound;

@Mixin(ClientWorld.class)
public abstract class ClientWorldSoundMixin {

    @Inject(
            method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nep$onPlayPacketSound(PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> soundEntry, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        SoundEvent sound = soundEntry.value();
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        if (!nep$shouldHandle(id)) {
            return;
        }

        if (nep$isMutedAmbient(id)) {
            ci.cancel();
            return;
        }

        SoundEvent replacement = nep$getReplacement(sound, id);
        if (replacement == null) {
            return;
        }

        RegistryEntry<SoundEvent> replacementEntry = Registries.SOUND_EVENT.getEntry(replacement);

        ci.cancel();
        ((ClientWorld) (Object) this).playSound(except, x, y, z, replacementEntry, category, volume, nep$limitAmbientPitch(id, pitch), seed);
    }

    @Inject(
            method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nep$onPlaySound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance, CallbackInfo ci) {
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        if (!nep$shouldHandle(id)) {
            return;
        }

        if (nep$isMutedAmbient(id)) {
            ci.cancel();
            return;
        }

        SoundEvent replacement = nep$getReplacement(sound, id);
        if (replacement == null) {
            return;
        }

        ci.cancel();
        ((ClientWorld) (Object) this).playSound(x, y, z, replacement, category, volume, nep$limitAmbientPitch(id, pitch), useDistance);
    }

    @Inject(
            method = "playSoundFromEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nep$onPlaySoundFromEntity(PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> soundEntry, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        SoundEvent sound = soundEntry.value();
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        if (!nep$shouldHandle(id)) {
            return;
        }

        if (nep$isMutedAmbient(id)) {
            ci.cancel();
            return;
        }

        SoundEvent replacement = nep$getReplacement(sound, id);
        if (replacement == null) {
            return;
        }

        RegistryEntry<SoundEvent> replacementEntry = Registries.SOUND_EVENT.getEntry(replacement);

        ci.cancel();
        ((ClientWorld) (Object) this).playSoundFromEntity(except, entity, replacementEntry, category, volume, nep$limitAmbientPitch(id, pitch), seed);
    }

    @Unique
    private static SoundEvent nep$getReplacement(SoundEvent sound, Identifier id) {
        if (!isPonyVillagerSoundsEnabled.get()) {
            return null;
        }

        SoundEvent replacement = getVillagerSound(id);
        if (replacement == null || replacement == sound) {
            return null;
        }

        return replacement;
    }

    @Unique
    private static boolean nep$shouldHandle(Identifier id) {
        return isPonyVillagerInOriginalModEnabled().get()
                && id != null
                && "minecraft".equals(id.getNamespace())
                && id.getPath().startsWith("entity.villager.");
    }

    @Unique
    private static boolean nep$isMutedAmbient(Identifier id) {
        return "entity.villager.ambient".equals(id.getPath()) && !isPonyVillagerAmbientSoundsEnabled.get();
    }

    @Unique
    private static float nep$limitAmbientPitch(Identifier id, float pitch) {
        if ("entity.villager.ambient".equals(id.getPath())) {
            return Math.max(0.9f, Math.min(pitch, 1.15f));
        }
        return pitch;
    }
}
