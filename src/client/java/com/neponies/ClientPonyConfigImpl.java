package com.neponies;

import com.neponies.NEPoniesConfig;
import com.neponies.VillagerPonyEntityAccessor;
import com.neponies.mixin.VillagerEntityMixin;
import com.neponies.util.PonyConfigBridge;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;

import static net.minecraft.entity.Entity.CUSTOM_NAME;

public class ClientPonyConfigImpl {

    public static void init() {
        PonyConfigBridge.CAN_SHOW_PROFESSION = ClientPonyConfigImpl::canShowProfession;
        PonyConfigBridge.CAN_SHOW_CUSTOM_NAME = ClientPonyConfigImpl::canShowCustomName;
    }

    private static boolean canShowProfession(VillagerPonyEntityAccessor villager) {
        if (!NEPoniesConfig.isProfessionInPonyCustomNamesEnabled.get()) return false;
        if (!NEPoniesConfig.isPonyVillagerInOriginalModEnabled().get()) return false;

        Text profession = villager.getProfessionName();
        return !profession.getString().isEmpty();
    }

    private static boolean canShowCustomName(VillagerPonyEntityAccessor accessor) {
        if (accessor instanceof Entity villager) {

            boolean hasVanillaCustomName = (villager.dataTracker.get(CUSTOM_NAME)).isPresent();
            if (!NEPoniesConfig.isPonyCustomNamesEnabled.get()) return false;
            if (!NEPoniesConfig.isPonyVillagerInOriginalModEnabled().get()) return false;
            if (hasVanillaCustomName) return false;

            Text name = accessor.getPonyCustomName();
            return name != null && !name.getString().isEmpty();
        }

        return false;
    }
}