package me.arycer.kerosmp.Mixin;

import me.arycer.kerosmp.Config.Language;
import me.arycer.kerosmp.Main;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    private final MinecraftServer server = Main.getServer();
    private Language language = Main.getConfig().getLanguage(player.getGameProfile());

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(CallbackInfo ci) {
        String message = "";
        switch (language) {
            case Spanish -> message = "Los mensajes de chat están desactivados en este servidor.";
            case English -> message = "Chat messages are disabled on this server.";
            default -> Main.LOGGER.error("Unknown language: " + language);
        }

        player.sendMessage(Main.createText(message, Formatting.GRAY, false), false);
        ci.cancel();
    }

    @Inject(method = "sleep", at = @At("HEAD"))
    public void sleep(CallbackInfo ci) {
        int connectedPlayers = server.getPlayerManager().getPlayerList().toArray().length;
        int sleepingPlayers = server.getPlayerManager().getPlayerList().stream()
                .filter(LivingEntity::isSleeping)
                .toArray().length + 1;

        String msg1 = "";
        switch (language) {
            case Spanish -> msg1 = " fue a dormir. (%d/%d)";
            case English -> msg1 = " went to sleep. (%d/%d)";
            default -> Main.LOGGER.error("Unknown language: " + language);
        }

        Text text = Main.createText(String.format(msg1, sleepingPlayers, connectedPlayers), Formatting.YELLOW, false);
        Text msg = player.getDisplayName().copy().append(text);

        String msg2 = "";
        switch (language) {
            case Spanish -> msg2 = " ¡Dulces sueños!";
            case English -> msg2 = ". Good night!";
            default -> Main.LOGGER.error("Unknown language: " + language);
        }

        if (sleepingPlayers >= connectedPlayers) {
            msg = msg.copy().append(Main.createText(msg2, Formatting.YELLOW, true));
        }

        server.getPlayerManager().broadcast(msg, false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        language = Main.getConfig().getLanguage(player.getGameProfile());

        ServerWorldProperties properties = Main.getServerWorldProperties();
        if (properties == null) return;

        if (!properties.isRaining() && !properties.isThundering()) return;

        String msg1 = "";
        switch (language) {
            case Spanish -> msg1 = "Quedan %s de %s";
            case English -> msg1 = "%s of %s left";
            default -> Main.LOGGER.error("Unknown language: " + language);
        }

        String msg2 = "";
        switch (language) {
            case Spanish -> msg2 = "La %s ha terminado";
            case English -> msg2 = "The %s has ended";
            default -> Main.LOGGER.error("Unknown language: " + language);
        }

        String msg3 = "";
        switch (language) {
            case Spanish -> msg3 = properties.isThundering() ? "tormenta" : "lluvia";
            case English -> msg3 = properties.isThundering() ? "thunderstorm" : "rain";
            default -> Main.LOGGER.error("Unknown language: " + language);
        }

        Text msg;
        if (properties.getRainTime() > 1) {
            msg = Main.createText(String.format(msg1, ticksToTime(properties.getRainTime()), msg3), Formatting.GRAY, false);
        } else {
            msg = Main.createText(String.format(msg2, msg3), Formatting.GRAY, false);
        }

        player.sendMessage(msg, true);
    }

    private static String ticksToTime(long ticks) {
        int seconds = (int) (ticks / 20);
        int minutes = seconds / 60;
        int hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}