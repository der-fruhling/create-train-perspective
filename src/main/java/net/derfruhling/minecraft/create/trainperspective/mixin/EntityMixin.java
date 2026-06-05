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

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.derfruhling.minecraft.create.trainperspective.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
@Implements({@Interface(iface = Perspective.class, prefix = "ctp$")})
@OnlyIn(Dist.CLIENT)
public abstract class EntityMixin {
    @Shadow
    @Nullable
    private Entity vehicle;
    @Shadow
    private Level level;

    @Unique
    private boolean ctp$perspectiveActive = false;
    @Unique
    private @Nullable CarriageContraptionEntity ctp$reference = null;
    @Unique
    private float ctp$scale = 1.0f;
    @Unique
    private float ctp$prevScale = 1.0f;
    @Unique
    private @Nullable RotationState ctp$currentState = null;

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "RETURN", ordinal = 4))
    public void onMount(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        var self = (Entity) (Object) this;
        if (Conditional.shouldApplyPerspectiveTo(self)) {
            CreateTrainPerspectiveMod.INSTANCE.onEntityMountEvent(true, self, entity);
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    public void onDismount(CallbackInfo ci) {
        if (vehicle != null) {
            var self = (Entity) (Object) this;
            if (Conditional.shouldApplyPerspectiveTo(self)) {
                CreateTrainPerspectiveMod.INSTANCE.onEntityMountEvent(false, self, vehicle);
            }
        }
    }

    @SuppressWarnings("UnreachableCode")
    @ModifyVariable(method = "calculateViewVector", at = @At(value = "LOAD"), index = 1, argsOnly = true)
    public float modifyPitch(float pitch, @Local(argsOnly = true, index = 2) float yaw) {
        if (this.level.isClientSide) {
            if (((Entity) (Object) this) instanceof Perspective persp
                    && persp.isEnabled()
                    && Conditional.shouldApplyPerspectiveTo((Entity) (Object) this)) {
                return MixinUtil.applyDirectionXRotChange(persp, pitch, yaw, 1.0f);
            } else return pitch;
        } else return pitch;
    }

    @SuppressWarnings("UnreachableCode")
    @ModifyVariable(method = "calculateViewVector", at = @At(value = "LOAD"), index = 2, argsOnly = true)
    public float modifyYaw(float yaw, @Local(argsOnly = true, index = 1) float pitch) {
        if (this.level.isClientSide) {
            if (((Entity) (Object) this) instanceof Perspective persp
                    && persp.isEnabled()
                    && Conditional.shouldApplyPerspectiveTo((Entity) (Object) this)) {
                return yaw + MixinUtil.getExtraYRot(persp, pitch, yaw, 1.0f);
            } else return yaw;
        } else return yaw;
    }

    public void ctp$enable(CarriageContraptionEntity entity, RotationState state) {
        ctp$perspectiveActive = true;
        ctp$currentState = state;
        ctp$reference = entity;
        ctp$prevScale = 1.0f;
        ctp$scale = 1.0f;
    }

    public void ctp$disable() {
        ctp$perspectiveActive = false;
        ctp$currentState = null;
        ctp$reference = null;
        ctp$prevScale = 1.0f;
        ctp$scale = 1.0f;
    }

    public void ctp$setReference(CarriageContraptionEntity entity) {
        ctp$reference = entity;
        ctp$prevScale = ctp$scale;
        ctp$scale = 1.0f;
    }

    public CarriageContraptionEntity ctp$getReference() {
        return ctp$reference;
    }

    public void ctp$diminish() {
        if (ctp$scale <= 0.0f) return;
        ctp$prevScale = ctp$scale;
        ctp$scale = Mth.lerp(0.1f, ctp$scale, 0.0f);
    }

    public float ctp$getValueScale() {
        return ctp$scale;
    }

    public float ctp$getPrevValueScale() {
        return ctp$prevScale;
    }

    public boolean ctp$isEnabled() {
        return ctp$perspectiveActive;
    }

    public @Nullable RotationState ctp$getRotationState() {
        return ctp$currentState;
    }
}
