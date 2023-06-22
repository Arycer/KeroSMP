package me.arycer.kerosmp.Mixin;

import me.arycer.kerosmp.Main;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    private final MinecraftServer server = Main.getServer();

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(CallbackInfo ci) {
        ci.cancel();

        player.sendMessage(Main.createText("Los mensajes de chat están desactivados en este servidor.", Formatting.GRAY, false), false);
    }

    @Inject(method = "sleep", at = @At("HEAD"))
    public void sleep(CallbackInfo ci) {
        int connectedPlayers = server.getPlayerManager().getPlayerList().toArray().length;
        int sleepingPlayers = server.getPlayerManager().getPlayerList().stream()
                .filter(LivingEntity::isSleeping)
                .toArray().length + 1;

        Text text = Main.createText(String.format(" fue a dormir. (%d/%d)", sleepingPlayers, connectedPlayers), Formatting.YELLOW, false);
        Text msg = player.getDisplayName().copy().append(text);

        if (sleepingPlayers >= connectedPlayers) {
            msg = msg.copy().append(Main.createText(" ¡Dulces sueños!", Formatting.YELLOW, true));
        }

        server.getPlayerManager().broadcast(msg, false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ServerWorldProperties properties = Main.getServerWorldProperties();
        if (properties == null) return;

        if (!properties.isRaining() && !properties.isThundering()) return;

        Text msg;
        if (properties.getRainTime() > 1) {
            msg = Main.createText(String.format("Quedan %s de %s",
                            ticksToTime(properties.getRainTime()),
                            properties.isThundering() ? "tormenta" : "lluvia"),
                    Formatting.GRAY, false);
        } else {
            msg = Main.createText(String.format("La %s ha terminado",
                            properties.isThundering() ? "tormenta" : "lluvia"),
                    Formatting.GRAY, false);
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