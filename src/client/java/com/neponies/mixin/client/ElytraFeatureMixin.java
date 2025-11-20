package com.neponies.mixin.client;

import com.minelittlepony.client.model.armour.ArmourLayer;
import com.minelittlepony.client.model.armour.ArmourRendererPlugin;
import com.minelittlepony.client.render.entity.feature.ElytraFeature;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraFeature.class)
public abstract class ElytraFeatureMixin<T extends LivingEntity> {

    /**
     *  Adds an additional Trinkets check so Elytra renders if equipped via Trinkets.
     */
    @Redirect(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/minelittlepony/client/model/armour/ArmourRendererPlugin;getArmorStacks(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;Lcom/minelittlepony/client/model/armour/ArmourLayer;Lcom/minelittlepony/client/model/armour/ArmourRendererPlugin$ArmourType;)[Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack[] redirectGetArmorStacks(ArmourRendererPlugin plugin, LivingEntity entity, EquipmentSlot slot, ArmourLayer layer, ArmourRendererPlugin.ArmourType type) {
        ItemStack[] stacks = plugin.getArmorStacks(entity, slot, layer, type);

        if (type == ArmourRendererPlugin.ArmourType.ELYTRA && slot == EquipmentSlot.CHEST && layer == ArmourLayer.OUTER) {
            boolean hasElytra = false;
            for (ItemStack stack : stacks) {
                if (stack.isOf(Items.ELYTRA)) {
                    hasElytra = true;
                    break;
                }
            }

            if (!hasElytra && entityHasElytraFromTrinkets(entity)) {
                // Append fake elytra stack to trigger rendering
                ItemStack[] newStacks = new ItemStack[stacks.length + 1];
                System.arraycopy(stacks, 0, newStacks, 0, stacks.length);
                newStacks[stacks.length] = new ItemStack(Items.ELYTRA);
                return newStacks;
            }
        }
        return stacks;
    }

    @Unique
    private static boolean entityHasElytraFromTrinkets(LivingEntity entity) {
        try {
            return TrinketsApi.getTrinketComponent(entity)
                    .map(comp -> !comp.getEquipped(stack -> stack.isOf(Items.ELYTRA)).isEmpty())
                    .orElse(false);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}