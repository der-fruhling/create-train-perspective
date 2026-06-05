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

package net.derfruhling.minecraft.create.trainperspective;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class MixinUtil {
    private MixinUtil() {
    }

    public static Camera3D asCamera3D(Camera camera) {
        return (Camera3D) camera;
    }

    public static float applyDirectionXRotChange(Perspective persp, float xRot, float yRot, float f) {
        return xRot - persp.getLean(f)
                * ModConfig.INSTANCE.leanMagnitude
                * Mth.sin((persp.getYaw(f) - yRot) * Mth.DEG_TO_RAD);
    }

    public static float getExtraYRot(Perspective persp, float xRot, float yRot, float f) {
        return persp.getLean(f) * Mth.sin(xRot * Mth.DEG_TO_RAD) * -Mth.cos((persp.getYaw(f) - yRot) * Mth.DEG_TO_RAD);
    }

    public static Vector3d applyStandingCameraTranslation(Player player, double x, double y, double z, Perspective persp, float f) {
        var lean = persp.getLean(f) * Mth.DEG_TO_RAD;
        var yaw = persp.getYaw(f) * Mth.DEG_TO_RAD;
        var height = player.getEyeHeight();

        if(player.getVehicle() != null) {
            height -= 1.5f;
        }

        var newY = y + ((height * Mth.cos(lean)) - height);
        var leanSin = Mth.sin(lean);
        var newZ = z - (height * Mth.sin(yaw) * leanSin);
        var newX = x - (height * Mth.cos(yaw) * leanSin);

        return new Vector3d(newX, newY, newZ);
    }

    public static Vec3 applyStandingCameraTranslation(Player player, Vec3 v, Perspective persp, float f) {
        var vec = applyStandingCameraTranslation(player, v.x, v.y, v.z, persp, f);
        return new Vec3(vec.x, vec.y, vec.z);
    }
}
