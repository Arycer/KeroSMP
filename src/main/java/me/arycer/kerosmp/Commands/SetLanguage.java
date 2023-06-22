package me.arycer.kerosmp.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.arycer.kerosmp.Config.Language;
import me.arycer.kerosmp.Main;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SetLanguage {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cmdRegistry, CommandManager.RegistrationEnvironment env) {
        dispatcher.register(CommandManager.literal("setlanguage")
                .then(CommandManager.argument("language", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("english");
                            builder.suggest("spanish");
                            return builder.buildFuture();
                        })
                        .executes(SetLanguage::execute)
                )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        ServerCommandSource source = serverCommandSourceCommandContext.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        String language = StringArgumentType.getString(serverCommandSourceCommandContext, "language").toLowerCase();
        Language lang;
        switch (language) {
            case "english" -> lang = Language.English;
            case "spanish" -> lang = Language.Spanish;
            default -> {
                Language currentLang = Main.getConfig().getLanguage(player.getGameProfile());
                String msg = "";
                switch (currentLang) {
                    case Spanish -> msg = "¡Idioma inválido! Elige entre español e inglés.";
                    case English -> msg = "Invalid language! Choose between Spanish and English.";
                    default -> Main.LOGGER.error("Unknown language: " + currentLang);
                }

                Text feedback = Main.createText(msg, Formatting.RED, false);
                player.sendMessage(feedback, false);
                return 0;
            }
        }

        Main.getConfig().setLanguage(player.getGameProfile(), lang);
        String msg = "";
        switch (lang) {
            case Spanish -> msg = "¡Idioma cambiado a español!";
            case English -> msg = "Language set to English!";
            default -> Main.LOGGER.error("Unknown language: " + lang);
        }

        Text feedback = Main.createText(msg, Formatting.GREEN, false);
        player.sendMessage(feedback, false);
        return 1;
    }
}
