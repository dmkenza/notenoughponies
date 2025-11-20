package com.neponies.mixin.client;

import com.minelittlepony.client.render.entity.AbstractPonyRenderer;
import com.neponies.VillagerPonyEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.neponies.Constants.*;
import static com.neponies.PonyUtils.canLoadCustomPonySkin;
import static com.neponies.PonyUtils.findCustomTexture;


@Mixin(value = AbstractPonyRenderer.class)
public abstract class AbstractPonyRendererMixin<T extends MobEntity>  extends EntityRenderer<T> {


    protected AbstractPonyRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    /**
     * Override the pony texture with a custom one
     */

    @Inject(
            method = "getTexture(Lnet/minecraft/entity/mob/MobEntity;)Lnet/minecraft/util/Identifier;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetTexture(T entity, CallbackInfoReturnable<Identifier> cir) {
        if (entity instanceof VillagerEntity villager && canLoadCustomPonySkin(villager)) {
            Identifier custom = findCustomTexture(villager);
            if (custom != null) {
                cir.setReturnValue(custom);
            }
        }
    }

    /**
     * A9X2QK
     * Make the baby label smaller
     */

    @Inject(
            method = "renderLabelIfPresent(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/MobEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void injectScaleAfterTranslate(T entity, Text name, MatrixStack stack, VertexConsumerProvider renderContext, int light, float tickDelta, CallbackInfo ci) {
        if (!(entity instanceof VillagerEntity villager)) return;
        if(villager.isBaby()){
            float scale =  BABY_LABEL_SCALE;
            stack.scale(scale, scale, scale);
        }
    }

    /**
     * #A9X2QK
     * Render villager pony profession if enabled.
     */
    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", at = @At("RETURN"), cancellable = true)
    private void renderProfessionLabel(T entity, Text name, MatrixStack stack,
                                       VertexConsumerProvider renderContext, int light, float tickDelta, CallbackInfo ci) {
        if (!(entity instanceof VillagerEntity villager)) return;

        VillagerPonyEntityAccessor pony = (VillagerPonyEntityAccessor) villager;
        if (!pony.canShowProfessionName()) return;

        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player.squaredDistanceTo(entity) > RENDER_PROFESSION_RADIUS){
            return;
        }

        Text professionText = pony.getProfessionName();
        stack.push();
        float scale = PROFESSION_LABEL_SCALE;
        stack.scale(scale, scale, scale);

        stack.translate(0, PROFESSION_Y_OFFSET, 0);
        super.renderLabelIfPresent(entity, professionText, stack, renderContext, light, tickDelta);
        stack.pop();
    }


}