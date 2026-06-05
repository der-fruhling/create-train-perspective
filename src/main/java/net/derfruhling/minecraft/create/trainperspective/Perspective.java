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

import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public interface Perspective {
    /**
     * Enables this perspective and attaches it to the provided carriage
     * contraption entity.
     *
     * @param entity The entity.
     */
    void enable(CarriageContraptionEntity entity, RotationState state);

    /**
     * Disables this perspective and resets it to the default state.
     */
    void disable();

    /**
     * @return {@code true} if enabled.
     */
    boolean isEnabled();

    /**
     * Sets the new reference carriage contraption entity for this perspective.
     *
     * @throws IllegalStateException If {@link #isEnabled()} returns {@code false}.
     * @param entity The new reference.
     */
    void setReference(CarriageContraptionEntity entity);

    /**
     * @return The reference carriage contraption entity, or {@code null} if
     * {@link #isEnabled()} would return {@code false}
     */
    @Nullable
    CarriageContraptionEntity getReference();

    /**
     * Calculates the amount of lean to apply to this perspective.
     *
     * @param f The delta between the last tick and the next tick.
     * @return Lean value.
     */
    default float getLean(float f) {
        var ref = getReference();
        if (ref == null) return 0.0f;
        if (f == 1.0f) return ref.pitch * getValueScale();
        return Mth.lerp(f, ref.prevPitch * getPrevValueScale(), ref.pitch * getValueScale());
    }

    /**
     * Figures out the yaw of the contraption on this particular frame.
     *
     * @param f The delta between the last tick and the next tick.
     * @return Yaw value.
     */
    default float getYaw(float f) {
        var ref = getReference();
        if (ref == null) return 0.0f;
        if (f == 1.0f) return ref.yaw * getValueScale();

        while (ref.yaw - ref.prevYaw < -180.0f) {
            ref.prevYaw -= 360.0f;
        }

        while (ref.yaw - ref.prevYaw >= 180.0f) {
            ref.prevYaw += 360.0f;
        }

        return Mth.lerp(f, ref.prevYaw * getPrevValueScale(), ref.yaw * getValueScale());
    }

    /**
     * @return The current {@link RotationState} value.
     */
    @Nullable
    RotationState getRotationState();

    /**
     * Applies a diminishing effect to this perspective, called every tick if
     * {@link RotationState#shouldTickState()} returns {@code false}.
     */
    void diminish();

    /**
     * @return The value scale calculated in the last tick.
     *         This will only be less than one if {@link #diminish()} was
     *         called last tick.
     */
    float getPrevValueScale();

    // this must not be named getScale()!!!!
    // Entity (or something) has a method also called getScale() that conflicts
    // with this method in a fuckey way.
    /**
     * @return The value scale calculated in this tick.
     *         This will only be less than one if {@link #diminish()} was
     *         called this tick.
     */
    float getValueScale();

    /**
     * @return {@code true} if {@link #diminish()} has been called enough to
     *         reduce the effect that the mod is having on the perspective that
     *         it looks <i>close enough</i> to vanilla to just {@link #disable()}
     *         this perspective and stop wasting time multiplying values forever.
     */
    default boolean isDiminished() {
        return getValueScale() < 0.00025f;
    }
}
