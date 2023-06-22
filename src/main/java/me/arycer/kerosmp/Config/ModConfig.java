package me.arycer.kerosmp.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ModConfig {
    private HashMap<String, Language> languages = new HashMap<>();

    public Language getLanguage(GameProfile profile) {
        String UUID = profile.getId().toString();
        if (!languages.containsKey(UUID)) {
            setLanguage(profile, Language.English);
        }

        return languages.get(UUID);
    }

    public void setLanguage(GameProfile profile, Language language) {
        String UUID = profile.getId().toString();
        if (languages.containsKey(UUID)) {
            languages.replace(UUID, language);
        } else {
            languages.put(UUID, language);
        }

        saveConfig();
    }

    public void readConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("languages.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (!Files.exists(path)) {
            try {
                Files.writeString(path, gson.toJson(this));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            ModConfig config = gson.fromJson(Files.readString(path), ModConfig.class);
            languages = config.languages;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("languages.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.writeString(path, gson.toJson(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
