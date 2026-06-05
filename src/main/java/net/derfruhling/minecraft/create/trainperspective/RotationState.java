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
import org.jetbrains.annotations.Nullable;

/**
 * Contains some state needed to support rotating the player based on a
 * carriage contraption's value.
 * This is here mostly for simplicity, otherwise there would be too many
 * {@code @Nullable} fields in
 * {@link net.derfruhling.minecraft.create.trainperspective.mixin.EntityMixin EntityMixin}
 */
public class RotationState {
    private CarriageContraptionEntity contraption;
    private boolean isStandingState;
    private float lastRecordedYaw;
    private boolean shouldTickState = true;
    private int ticksSinceLastUpdate = 0;

    /**
     * Constructs a new rotation state.
     * This constructor is used when an entity is mounting a train.
     *
     * @param contraption The contraption to bind to.
     * @param isStandingState If {@code true} then the entity is standing on
     *                        the train and requires different logic to
     *                        detect when the player leaves the train.
     *                        <p>
     *                        Otherwise, the player is seated, and therefore
     *                        it can be assumed that when the player leaves
     *                        their seat they will probably continue to stand
     *                        for at least a moment.
     *                        <p>
     *                        As such, the state is updated to be a
     *                        standing state, which correctly handles if the
     *                        player dismounts into a position which is no
     *                        longer on the train.
     */
    public RotationState(CarriageContraptionEntity contraption, boolean isStandingState) {
        this.contraption = contraption;
        lastRecordedYaw = contraption.yaw;
        this.isStandingState = isStandingState;
    }

    /**
     * @return The difference between the current yaw of the contraption and
     *         the last yaw recorded by this function.
     * @implNote This method does not take into account mid-tick frames, and
     *           as such should not be used for rendering.
     */
    public float getYawDelta() {
        while (contraption.yaw - lastRecordedYaw < -180.0f) {
            lastRecordedYaw -= 360.0f;
        }

        while (contraption.yaw - lastRecordedYaw >= 180.0f) {
            lastRecordedYaw += 360.0f;
        }

        var rotation = contraption.yaw - lastRecordedYaw;
        lastRecordedYaw = contraption.yaw;
        return rotation;
    }

    /**
     * @return The currently bound contraption.
     */
    public @Nullable CarriageContraptionEntity getContraption() {
        return contraption;
    }

    /**
     * @return {@code true} if this is a standing state,
     *         {@code false} otherwise.
     */
    public boolean isStanding() {
        return isStandingState;
    }

    /**
     * @return {@code true} if this is <b>not</b> a standing state,
     *         {@code false} otherwise.
     */
    public boolean isSeated() {
        return !isStandingState;
    }

    /**
     * If {@link #isStanding isStanding()} is true, then after the player is
     * detected not to be on the contraption this method will start returning
     * {@code false}.
     * Otherwise, it will always return {@code true}.
     *
     * @return {@code true} if this standing state should continue being ticked.
     */
    public boolean shouldTickState() {
        return shouldTickState;
    }

    /**
     * Called when the player begins riding the contraption.
     * Converts this state into a seated state.
     */
    public void onMounted() {
        this.isStandingState = false;

        // ensure correct behavior (this is ignored for seated states anyway)
        this.shouldTickState = true;
    }

    /**
     * Called when the player stops riding the contraption.
     * Converts this state into a standing state.
     */
    public void onDismount() {
        this.isStandingState = true;
    }

    /**
     * Calls for this state to stop ticking.
     *
     * @throws IllegalStateException Not {@link #isStanding()}
     */
    public void stopTickingState() {
        if(!isStandingState) throw new IllegalStateException("not a standing state");

        this.shouldTickState = false;
    }

    /**
     * @return Returns the number of {@link #tick()} calls since the last
     *         {@link #update()}
     */
    public int getTicksSinceLastUpdate() {
        return ticksSinceLastUpdate;
    }

    void update() {
        ticksSinceLastUpdate = 0;
    }

    void tick() {
        ticksSinceLastUpdate += 1;
    }
}
