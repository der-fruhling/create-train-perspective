package net.derfruhling.minecraft.create.trainperspective;

import com.mojang.logging.LogUtils;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateTrainPerspectiveNeo.MODID)
public class CreateTrainPerspectiveNeo {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "create_train_perspective";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateTrainPerspectiveNeo(IEventBus modEventBus, ModContainer modContainer) {
        if(ModList.get().isLoaded("cloth_config")) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (container, screen) -> ModConfigScreenFactory.createConfigScreen(screen));
        }

        CreateTrainPerspectiveMod.INSTANCE = new CreateTrainPerspectiveMod();
        modEventBus.addListener(this::onFMLClientSetup);
        NeoForge.EVENT_BUS.register(this);
    }

    public void onFMLClientSetup(FMLClientSetupEvent event) {
        var client = CClient.class;
        if(ModConfig.INSTANCE.disableRotateWhenSeated) {
            try {
                var field = client.getField("rotateWhenSeated");
                field.setAccessible(true); // avoid issues
                var value = (ConfigBase.ConfigBool) field.get(AllConfigs.client());
                value.set(false);
                LOGGER.warn("Workaround applied: disabled rotateWhenSeated in Create's config as it conflicts with this mod's functionality");
            } catch (NoSuchFieldException e) {
                // field does not exist, probably just an older version of create
                ModConfig.INSTANCE.isRotateWhenSeatedAvailable = false;
                LOGGER.info("No such config option: rotateWhenSeated; probably using older version of Create, which is fine! Hooray!");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.warn("Not applying rotateWhenSeated workaround; don't blame me if you're rotating too much");
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        ModConfig.tick();
    }
}
