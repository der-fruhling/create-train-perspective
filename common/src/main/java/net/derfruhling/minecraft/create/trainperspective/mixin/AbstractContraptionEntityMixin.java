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

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.derfruhling.minecraft.create.trainperspective.Conditional;
import net.derfruhling.minecraft.create.trainperspective.CreateTrainPerspectiveMod;
import net.derfruhling.minecraft.create.trainperspective.Perspective;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContraptionEntity.class)
@Environment(EnvType.CLIENT)
public class AbstractContraptionEntityMixin {
    @SuppressWarnings({"ConstantValue", "UnreachableCode"})
    @Inject(method = "registerColliding", at = @At("TAIL"), remap = false)
    private void onRegisterColliding(
            Entity collidingEntity,
            CallbackInfo ci
    ) {
        if (!collidingEntity.level().isClientSide) return;
        if ((Object) this instanceof CarriageContraptionEntity carriage
                && collidingEntity instanceof Perspective
                && Conditional.shouldApplyPerspectiveTo(collidingEntity)) {
            CreateTrainPerspectiveMod.INSTANCE.tickStandingEntity(carriage, collidingEntity);
        }
    }
}
