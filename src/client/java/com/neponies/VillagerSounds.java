package com.neponies;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.neponies.NEPoniesConfig.isPonyVillagerSoundsEnabled;


public class VillagerSounds {

    private static final Logger logger = LoggerFactory.getLogger("notenoughponies");
    public static final String MOD_ID = "notenoughponies";

    // List of vanilla sound suffixes to replace
    public static final Set<String> REPLACED_VILLAGER_SOUND_EVENTS = Set.of(
            "ambient", "death", "hurt", "yes", "no", "trade"
    );

    // Registered custom villager sound IDs
    public static final List<Identifier> VILLAGERS_SOUNDS = new ArrayList<>();

    /**
     * Registers all custom villager sound events defined in sounds.json
     */
    public static void registerVillagerSounds() {
        for (String eventName : REPLACED_VILLAGER_SOUND_EVENTS) {
            Identifier id = Identifier.of(MOD_ID, "entity.villager." + eventName + ".nep");
            Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
            VILLAGERS_SOUNDS.add(id);
        }
        logger.info("Registered {} custom villager sound events.", VILLAGERS_SOUNDS.size());
    }

    /**
     * Returns a replacement sound for a vanilla villager sound
     * @param originalId original vanilla sound ID
     * @return replacement SoundEvent if found, else original
     */
    public static SoundEvent getVillagerSound(Identifier originalId) {
        if (!isPonyVillagerSoundsEnabled.get()) {
            return Registries.SOUND_EVENT.get(originalId);
        }

        String path = originalId.getPath();
        if (!path.startsWith("entity.villager.")) {
            return Registries.SOUND_EVENT.get(originalId);
        }

        String suffix = path.substring("entity.villager.".length());
        if (REPLACED_VILLAGER_SOUND_EVENTS.contains(suffix)) {
            Identifier nepId = Identifier.of(MOD_ID, "entity.villager." + suffix + ".nep");
            return Registries.SOUND_EVENT.getOrEmpty(nepId)
                    .orElse(Registries.SOUND_EVENT.get(originalId));
        }

        return Registries.SOUND_EVENT.get(originalId);
    }
}