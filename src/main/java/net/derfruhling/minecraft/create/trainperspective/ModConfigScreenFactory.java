package net.derfruhling.minecraft.create.trainperspective;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class ModConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.create_train_perspective.config"))
                .setSavingRunnable(ModConfig.INSTANCE::save);

        var general = builder.getOrCreateCategory(Component.translatable(
                "category.create_train_perspective.general"
        ));

        var entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.enabled"),
                        ModConfig.INSTANCE.enabled)
                .setTooltip(Component.translatable("option.create_train_perspective.enabled.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.enabled = value)
                .setDefaultValue(true)
                .build());

        var leaning = entryBuilder.startSubCategory(Component.translatable(
                "category.create_train_perspective.leaning"
        ));

        leaning.add(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.leaning.enabled"),
                        ModConfig.INSTANCE.leanEnabled)
                .setTooltip(Component.translatable("option.create_train_perspective.leaning.enabled.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.leanEnabled = value)
                .setDefaultValue(true)
                .build());

        leaning.add(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.leaning.roll_enabled"),
                        ModConfig.INSTANCE.rollEnabled)
                .setTooltip(Component.translatable("option.create_train_perspective.leaning.roll_enabled.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.rollEnabled = value)
                .setDefaultValue(true)
                .build());

        general.addEntry(leaning.build());

        var multiplayer = entryBuilder.startSubCategory(Component.translatable(
                "category.create_train_perspective.multiplayer"
        ));

        multiplayer.add(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.multiplayer.apply_to_others"),
                        ModConfig.INSTANCE.applyToOthers)
                .setTooltip(Component.translatable("option.create_train_perspective.multiplayer.apply_to_others.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.applyToOthers = value)
                .setDefaultValue(true)
                .build());

        general.addEntry(multiplayer.build());

        var advanced = entryBuilder.startSubCategory(Component.translatable(
                "category.create_train_perspective.advanced"
        ));

        advanced.add(entryBuilder
                .startFloatField(
                        Component.translatable("option.create_train_perspective.advanced.lean_magnitude"),
                        ModConfig.INSTANCE.leanMagnitude)
                .setTooltip(Component.translatable("option.create_train_perspective.advanced.lean_magnitude.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.leanMagnitude = value)
                .setDefaultValue(1.0f)
                .build());

        advanced.add(entryBuilder
                .startFloatField(
                        Component.translatable("option.create_train_perspective.advanced.roll_magnitude"),
                        ModConfig.INSTANCE.rollMagnitude)
                .setTooltip(Component.translatable("option.create_train_perspective.advanced.roll_magnitude.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.rollMagnitude = value)
                .setDefaultValue(1.0f)
                .build());

        advanced.add(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.advanced.apply_to_entities"),
                        ModConfig.INSTANCE.applyToNonPlayerEntities)
                .setTooltip(Component.translatable("option.create_train_perspective.advanced.apply_to_entities.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.applyToNonPlayerEntities = value)
                .setDefaultValue(true)
                .build());

        advanced.add(entryBuilder
                .startStrList(
                        Component.translatable("option.create_train_perspective.advanced.blocked_entities"),
                        ModConfig.INSTANCE.blockedEntities.stream().map(ResourceLocation::toString).toList())
                .setTooltip(Component.translatable("option.create_train_perspective.advanced.blocked_entities.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.blockedEntities = value.stream().map(ResourceLocation::parse).toList())
                .setDefaultValue(new ArrayList<>())
                .build());

        advanced.add(entryBuilder
                .startStrList(
                        Component.translatable("option.create_train_perspective.advanced.blocked_players"),
                        ModConfig.INSTANCE.blockedPlayerUUIDs.stream().map(UUID::toString).toList())
                .setTooltip(Component.translatable("option.create_train_perspective.advanced.blocked_players.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.blockedPlayerUUIDs = value.stream().map(UUID::fromString).toList())
                .setCellErrorSupplier(s -> {
                    try {
                        UUID.fromString(s);
                        return Optional.empty();
                    } catch (IllegalArgumentException e) {
                        return Optional.of(Component.translatable("option.create_train_perspective.advanced.blocked_players.error", e.getLocalizedMessage()));
                    }
                })
                .setDefaultValue(new ArrayList<>())
                .build());

        //noinspection UnstableApiUsage
        advanced.add(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.advanced.disable_rotate_when_seated"),
                        ModConfig.INSTANCE.disableRotateWhenSeated
                )
                .setTooltip(Component.translatable("option.create_train_perspective.advanced.disable_rotate_when_seated.tooltip"))
                .setSaveConsumer(value -> ModConfig.INSTANCE.disableRotateWhenSeated = value)
                .setDisplayRequirement(() -> ModConfig.INSTANCE.isRotateWhenSeatedAvailable)
                .setDefaultValue(true)
                .build());

        var debug = entryBuilder.startSubCategory(Component.translatable("category.create_train_perspective.debug"));

        debug.add(entryBuilder
                .startTextDescription(Component.translatable("category.create_train_perspective.debug.description").withStyle(ChatFormatting.BOLD))
                .build());

        debug.add(entryBuilder
                .startEnumSelector(
                        Component.translatable("option.create_train_perspective.debug.debug_mode"),
                        DebugMode.class,
                        ModConfig.INSTANCE.debugMode)
                .setSaveConsumer(value -> ModConfig.INSTANCE.debugMode = value)
                .setTooltip(Component.translatable("option.create_train_perspective.debug.debug_mode"))
                .setEnumNameProvider(m -> Component.translatable("option.create_train_perspective.debug.debug_mode." + m.name().toLowerCase(Locale.US)))
                .setDefaultValue(DebugMode.NONE)
                .build());

        debug.add(entryBuilder
                .startBooleanToggle(
                        Component.translatable("option.create_train_perspective.debug.enable_yaw_lock"),
                        ModConfig.INSTANCE.debugEnableYawLock
                )
                .setSaveConsumer(value -> ModConfig.INSTANCE.debugEnableYawLock = value)
                .setTooltip(Component.translatable("option.create_train_perspective.debug.enable_yaw_lock.tooltip"))
                .setDefaultValue(false)
                .build());

        debug.add(entryBuilder
                .startFloatField(
                        Component.translatable("option.create_train_perspective.debug.yaw_lock"),
                        ModConfig.INSTANCE.debugYawLock
                )
                .setSaveConsumer(value -> ModConfig.INSTANCE.debugYawLock = value)
                .setTooltip(Component.translatable("option.create_train_perspective.debug.yaw_lock.tooltip"))
                .setDefaultValue(90.0f)
                .build());

        advanced.add(debug.build());
        general.addEntry(advanced.build());

        return builder.build();
    }
}
