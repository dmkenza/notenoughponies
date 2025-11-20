package com.neponies;

import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

public class PonyComponentInitializer implements EntityComponentInitializer {

    public static final ComponentKey<PonyComponent> PONY_DATA =
            ComponentRegistry.getOrCreate(Identifier.of(NotEnoughPonies.MOD_ID, "pony_data"), PonyComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(VillagerEntity.class, PONY_DATA, villager -> new PonyComponent());
    }
}