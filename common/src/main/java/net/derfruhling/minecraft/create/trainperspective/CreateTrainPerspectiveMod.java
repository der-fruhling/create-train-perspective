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
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class CreateTrainPerspectiveMod {
    public static final String MODID = "create_train_perspective";
    public static CreateTrainPerspectiveMod INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTrainPerspectiveMod.class);

    public CreateTrainPerspectiveMod() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
            var client = CClient.class;
            if(ModConfig.INSTANCE.disableRotateWhenSeated) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    var field = client.getField("rotateWhenSeated");
                    field.setAccessible(true); // avoid issues
                    var value = field.get(AllConfigs.client());
                    var valueClass = value.getClass();
                    valueClass.getMethod("set", boolean.class).invoke(value, false);
                    LOGGER.warn("Workaround applied: disabled rotateWhenSeated in Create's config as it conflicts with this mod's functionality");
                } catch (NoSuchFieldException | NoSuchMethodException e) {
                    // field does not exist, probably just an older version of create
                    ModConfig.INSTANCE.isRotateWhenSeatedAvailable = false;
                    LOGGER.info("No such config option: rotateWhenSeated; probably using older version of Create, which is fine! Hooray!");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.warn("Not applying rotateWhenSeated workaround; don't blame me if you're rotating too much");
            }
        });

        ClientTickEvent.CLIENT_PRE.register(instance -> {
            ModConfig.tick();
        });

        INSTANCE = this;
    }

    public void onEntityMountEvent(boolean isMounting, Entity entityMounting, Entity entityBeingMounted) {
        if (
                entityMounting instanceof Perspective persp &&
                        entityBeingMounted instanceof CarriageContraptionEntity contraption
        ) {
            if (isMounting) {
                onEntityMount(persp, contraption);
            } else {
                onEntityDismount(persp);
            }
        }
    }

    public void onEntityMount(Perspective persp, CarriageContraptionEntity contraption) {
        if (persp.getRotationState() == null) {
            var state = new RotationState(contraption, false);
            persp.enable(contraption, state);
        } else {
            var state = persp.getRotationState();
            state.onMounted();
        }
    }

    private void onEntityDismount(Perspective persp) {
        if (persp.getRotationState() != null) {
            persp.getRotationState().onDismount();
        }
    }

    public void tickStandingEntity(final CarriageContraptionEntity contraption, final Entity entity) {
        if (entity.getVehicle() != null) return;
        if (!(entity instanceof Perspective persp)) return;

        var state = persp.getRotationState();

        if (state == null || !Objects.equals(state.getContraption(), contraption)) {
            state = new RotationState(contraption, true);
            persp.enable(contraption, state);
        } else {
            state.update();
        }
    }

    private void tickPerspectiveState(Entity player, Perspective persp, RotationState state) {
        var carriage = state.getContraption();
        if (carriage == null) return;
        persp.setReference(carriage);
        player.setYRot(player.getYRot() + state.getYawDelta());
        player.setYBodyRot(player.getYRot());

        if (state.isStanding() && !state.isSeated()) {
            state.tick();

            if (state.getTicksSinceLastUpdate() > 5) {
                state.stopTickingState();
            }
        }
    }

    public void tickEntity(Entity entity, Perspective persp) {
        if (persp.getRotationState() != null) {
            var state = persp.getRotationState();
            assert state != null;

            if (state.shouldTickState()) {
                tickPerspectiveState(entity, persp, state);
            } else {
                persp.diminish();

                if (persp.isDiminished()) {
                    persp.disable();
                }
            }
        }
    }
}
