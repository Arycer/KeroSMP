package me.arycer.kerosmp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer {
    public static final String MOD_ID = "kerosmp";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static MinecraftServer server;
    public static MinecraftServer getServer() {
        return server;
    }

    public static Text createText(String text, Formatting color, boolean bold) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(color).withBold(bold));
    }

    public static ServerWorldProperties getServerWorldProperties() {
        if (server == null) return null;

        World world = server.getWorld(World.OVERWORLD);
        if (world == null) return null;

        RegistryKey<World> worldKey = world.getRegistryKey();
        ServerWorld serverWorld = server.getWorld(worldKey);
        if (serverWorld == null) return null;

        return (ServerWorldProperties) serverWorld.getLevelProperties();
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> Main.server = server);

        LOGGER.info("Kero SMP has been initialized!");
    }
}
