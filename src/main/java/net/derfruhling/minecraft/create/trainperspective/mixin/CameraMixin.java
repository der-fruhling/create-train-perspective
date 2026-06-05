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

import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.derfruhling.minecraft.create.trainperspective.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

// workaround for figura to work (this is terrible but works)
@Mixin(value = Camera.class, priority = 1100)
@Implements({@Interface(iface = Camera3D.class, prefix = "c3d$")})
@OnlyIn(Dist.CLIENT)
public abstract class CameraMixin {
    @Unique
    private float ctp$extraYRot;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float zRot);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Unique
    public float c3d$getExtraYRot() {
        return this.ctp$extraYRot;
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", ordinal = 0))
    public void modifyRotations(Camera instance,
                                float yRot,
                                float xRot,
                                float zRot,
                                BlockGetter level,
                                Entity entity,
                                boolean detached,
                                boolean thirdPersonReverse,
                                float partialTick) {
        if (entity instanceof Perspective persp
                && Conditional.shouldApplyPerspectiveTo(entity)
                && Conditional.shouldApplyLeaning()
                && !detached) {
            if (ModConfig.INSTANCE.debugEnableYawLock) yRot = ModConfig.INSTANCE.debugYawLock;

            if (Conditional.shouldApplyRolling()) {
                zRot += persp.getLean(partialTick)
                        * ModConfig.INSTANCE.rollMagnitude
                        * Mth.cos((persp.getYaw(partialTick) - yRot) * Mth.DEG_TO_RAD)
                        * Mth.cos(xRot * Mth.DEG_TO_RAD)
                        * 0.5f;
            }

            ctp$extraYRot = MixinUtil.getExtraYRot(persp, xRot, yRot, partialTick);
            var newX = MixinUtil.applyDirectionXRotChange(persp, xRot, yRot, partialTick);

            if(ModConfig.INSTANCE.debugMode == DebugMode.SHOW_CAMERA_ROTATION) {
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.displayClientMessage(Component.literal(String.format(
                        "%.03f, %.03f (%.03f), %.03f",
                        newX,
                        yRot,
                        ctp$extraYRot,
                        zRot
                )), true);
            }

            setRotation(
                    yRot,
                    newX,
                    zRot
            );
        } else {
            ctp$extraYRot = 0;
            setRotation(yRot, xRot, zRot);
        }
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    public void modifyPosition(Camera instance,
                               double x,
                               double y,
                               double z,
                               BlockGetter level,
                               Entity entity,
                               boolean detached,
                               boolean thirdPersonReverse,
                               float partialTick) {
        if (entity instanceof AbstractClientPlayer clientPlayer
                && Conditional.shouldApplyPerspectiveTo(entity)
                && Conditional.shouldApplyLeaning()
                && !detached) {
            var persp = (Perspective) clientPlayer;
            var newV = MixinUtil.applyStandingCameraTranslation(clientPlayer, x, y, z, persp, partialTick);

            if (ModConfig.INSTANCE.debugMode == DebugMode.SHOW_STANDING_TRANSFORMS) {
                clientPlayer.displayClientMessage(Component.literal("%f, %f, %f".formatted(x - newV.x, y - newV.y, z - newV.z)), true);
            }

            setPosition(newV.x, newV.y, newV.z);
        } else {
            setPosition(x, y, z);
        }
    }
}
