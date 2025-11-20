package com.neponies;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public class PonyComponent implements Component, AutoSyncedComponent {
    private VillagerCustomPonyData data = new VillagerCustomPonyData();

    public VillagerCustomPonyData getData() {
        return data;
    }

    public void setData(VillagerCustomPonyData data) {
        this.data = data;
    }

    public NEPRace getRace() {
        return data.getRace();
    }

    public String getSkinID() {
        return data.getSkinID();
    }

    public String getFirstName() {
        return data.getFirstName();
    }

    public String getSecondName() {
        return data.getSecondName();
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.data = VillagerCustomPonyData.readNbt(tag);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.data.writeNbt(tag);
    }
}