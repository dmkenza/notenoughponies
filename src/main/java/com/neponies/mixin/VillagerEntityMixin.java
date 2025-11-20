package com.neponies.mixin;

import com.minelittlepony.api.pony.meta.Race;
import com.neponies.*;
import com.neponies.util.PonyConfigBridge;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.village.VillagerData;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import static com.neponies.VillagerCustomPonyData.PONIES_SKINS_COUNT;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements VillagerPonyEntityAccessor {

    @Unique
    private Text ponyCustomName;

    @Shadow
    public abstract VillagerData getVillagerData();

    @Unique
    private static VillagerEntity parentVillager1;

    @Unique
    private static VillagerEntity parentVillager2;

    @Unique
    private Runnable onInitializeListener;

    @Unique
    private boolean ponyDataChecked = false;

    @Unique
    private final ComponentKey<PonyComponent> PONY_DATA_KEY = PonyComponentInitializer.PONY_DATA;

    @Unique
    public VillagerCustomPonyData getVillagerCustomData() {
        return PONY_DATA_KEY.get(this).getData();
    }

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> type, World world) {
        super(type, world);
    }


    @Unique
    @Override
    public String getPonySkinID() {
        return getVillagerCustomData().getSkinID();
    }
    @Unique
    @Override
    public Text getPonyCustomName() {
        if (ponyCustomName != null) return ponyCustomName;

        String fullName = getFirstName() + " " + getSecondName();
        fullName = fullName.trim();

        ponyCustomName = Text.literal(fullName);
        return ponyCustomName;
    }


    @Unique
    @Override
    public Text getProfessionName() {

        String profession = getVillagerData().getProfession().toString();
        if (profession.equalsIgnoreCase("none")) {
            return Text.empty();
        }

        profession = upperFirstLetter(profession);
        return Text.literal(profession);
    }

    @Unique
    @Override
    public boolean canShowProfessionName() {
        if (this.getWorld().isClient) {
            return PonyConfigBridge.CAN_SHOW_PROFESSION.test(this);
        }
        return false;
    }

    @Unique
    public boolean canShowCustomPonyName() {
        if (this.getWorld().isClient) {
            return PonyConfigBridge.CAN_SHOW_CUSTOM_NAME.test(this);
        }
        return false;
    }

    @Unique
    @Override
    public void setPonyRace(NEPRace race) {
        getVillagerCustomData().setRace(race);
        PONY_DATA_KEY.sync(this);
    }


    @Unique
    @Override
    public NEPRace getPonyRace() {
        return getVillagerCustomData().getRace();
    }
    @Unique
    public String getFirstName() {
        return getVillagerCustomData().getFirstName();
    }
    @Unique
    public String getSecondName() {
        return getVillagerCustomData().getSecondName();
    }
    @Unique
    public String getPonyName() {
        return (getFirstName() + " " + getSecondName()).trim();
    }

    @Override
    public void checkAndSetRace() {
        if (this.getWorld().isClient) return;

        VillagerCustomPonyData data = getVillagerCustomData();
        boolean changed = false;

        UUID uuid = this.getUuid();

        String skinId = data.getSkinID();
        if (skinId == null) {
            data.setSkinID(Integer.toString(Math.abs(uuid.hashCode()) % PONIES_SKINS_COUNT));
            changed = true;
        }

        NEPRace babyRace = initPonyBaby();
        NEPRace race = (babyRace != null) ? babyRace : data.getRace();

        if (race == NEPRace.HUMAN) {
            int hash = Math.abs(uuid.hashCode());
            int chance = hash % 100; // 0..99
            if (chance < 25) {
                race = NEPRace.EARTH;        // 25%
            } else if (chance < 26) {
                race = NEPRace.ALICORN;      // 1%
            } else if (chance < 63) {
                race = NEPRace.PEGASUS;      // 37%
            } else {
                race = NEPRace.UNICORN;      // 37%
            }
            data.setRace(race);
            changed = true;
        }

        String first = data.getFirstName();
        if (first.isEmpty()) {
            data.setFirstName(PonyNames.generateFirstName(uuid));
            changed = true;
        }

        String second = data.getSecondName();
        if (second.isEmpty()) {
            data.setSecondName(PonyNames.generateSecondName(uuid));
            changed = true;
        }

        if (changed) {
            PONY_DATA_KEY.sync(this);
        }
    }

    @Unique
    private NEPRace initPonyBaby() {
        if (parentVillager1 != null && parentVillager2 != null) {
            NEPRace race = Math.random() < 0.5
                    ? ((VillagerPonyEntityAccessor) parentVillager1).getPonyRace()
                    : ((VillagerPonyEntityAccessor) parentVillager2).getPonyRace();

            VillagerPonyEntityAccessor self = (VillagerPonyEntityAccessor) (Object) this;
            self.setPonyRace(race);

            parentVillager1 = null;
            parentVillager2 = null;

            return race;
        }

        return null;
    }

    @Override
    public void setOnInitializeListener(Runnable listener) {
        this.onInitializeListener = listener;
    }

    @Inject(method = "createChild", at = @At("HEAD"))
    private void beforeCreateChild(ServerWorld serverWorld, PassiveEntity passiveEntity, CallbackInfoReturnable<VillagerEntity> cir) {
        parentVillager1 = (VillagerEntity)(Object)this;
        parentVillager2 = (VillagerEntity) passiveEntity;
    }

    @Inject(method = "initialize", at = @At("TAIL"))
    public void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        checkAndSetRace();

        if (onInitializeListener != null) {
            onInitializeListener.run();
            onInitializeListener = null;
        }
    }


    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // check data in case the mod was added to an old world
        if (!this.getWorld().isClient() && !this.ponyDataChecked) {
            PONY_DATA_KEY.maybeGet(this).ifPresent(component -> {
                if (getVillagerCustomData().getSkinID() == null) {
                    checkAndSetRace();
                }
                this.ponyDataChecked = true;
            });
        }
    }


    @Unique
    private static String upperFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


}