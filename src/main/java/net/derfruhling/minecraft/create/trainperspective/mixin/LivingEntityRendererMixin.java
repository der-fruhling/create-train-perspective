/*
 * Part of the Create: Train Perspective project.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 der_frühling
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.derfruhling.minecraft.create.trainperspective.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.derfruhling.minecraft.create.trainperspective.Conditional;
import net.derfruhling.minecraft.create.trainperspective.Perspective;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
@OnlyIn(Dist.CLIENT)
public class LivingEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V"))
    protected void render(LivingEntity entity, float p_115309_, float partialTick, PoseStack poseStack, MultiBufferSource p_115312_, int p_115313_, CallbackInfo ci) {
        if (Conditional.shouldApplyPerspectiveTo(entity)) {
            Perspective persp = (Perspective) entity;

            var lean = persp.getLean(partialTick);
            var yaw = persp.getYaw(partialTick);

            poseStack.translate(0, entity.getEyeHeight(), 0);

            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.cos(Mth.DEG_TO_RAD * yaw) * lean));
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(Mth.DEG_TO_RAD * yaw) * -lean));

            poseStack.translate(0, -entity.getEyeHeight(), 0);
        }
    }
}
