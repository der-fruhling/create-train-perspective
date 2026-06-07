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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final WatchService WATCH_SERVICE;
    private static final Path PATH = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("create-train-perspective.json");
    public static ModConfig INSTANCE = loadConfig();

    static {
        try {
            WATCH_SERVICE = FileSystems.getDefault().newWatchService();
            PATH.getParent().register(WATCH_SERVICE, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean enabled = true;
    public boolean leanEnabled = true;
    public float leanMagnitude = 1.0f;
    public boolean rollEnabled = true;
    public float rollMagnitude = 1.0f;
    public boolean applyToOthers = true;
    public boolean applyToNonPlayerEntities = true;
    public transient List<? extends EntityType<?>> blockedEntities = new ArrayList<>();
    public List<ResourceLocation> blockedEntityUUIDs = new ArrayList<>();
    public List<UUID> blockedPlayerUUIDs = new ArrayList<>();
    public DebugMode debugMode = DebugMode.NONE;
    public boolean disableRotateWhenSeated = true;
    public transient boolean isRotateWhenSeatedAvailable = true;
    public boolean debugEnableYawLock = false;
    public float debugYawLock = 90.0f;

    private ModConfig() {
    }

    public static void tick() {
        var key = WATCH_SERVICE.poll();

        if (key != null) {
            for (var event : key.pollEvents()) {
                if (event.context().toString().equals(PATH.getFileName().toString())) {
                    INSTANCE = loadConfig();
                    key.reset();
                }
            }
        }
    }

    private static ModConfig loadConfig() {
        if (Files.exists(PATH)) {
            try {
                var configJson = Files.readString(PATH);
                return GSON.fromJson(configJson, ModConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            var value = new ModConfig();
            value.save();
            return value;
        }
    }

    public void save() {
        try (var file = Files.newBufferedWriter(PATH)) {
            GSON.toJson(this, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
