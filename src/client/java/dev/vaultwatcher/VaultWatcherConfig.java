package dev.vaultwatcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Простой JSON-конфиг мода. Хранится в config/vaultwatcher.json
 */
public class VaultWatcherConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;

    // --- настройки, которые сохраняются ---
    public boolean enabled = true;

    // автоматически кликать ПКМ по vault/ominous vault, если смотришь на него
    // и держишь подходящий ключ
    public boolean openVaults = true;

    public boolean heavyCore = true;      // "навершие булавы"
    public boolean ominousBottle = true;  // "зловещая бутылочка"
    public boolean windCharge = true;     // "заряд ветра"

    public boolean density4 = true;    // зачарованная книга: Плотность IV
    public boolean density5 = true;    // зачарованная книга: Плотность V
    public boolean windBurst2 = true;  // зачарованная книга: Порыв ветра II
    public boolean windBurst3 = true;  // зачарованная книга: Порыв ветра III

    // список пользовательских id предметов, вида "minecraft:trident"
    public List<String> customItems = new ArrayList<>();

    public static void init(Path modConfigDir) {
        configPath = modConfigDir.resolve("vaultwatcher.json");
    }

    public static VaultWatcherConfig load() {
        try {
            if (configPath != null && Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                    VaultWatcherConfig cfg = GSON.fromJson(reader, VaultWatcherConfig.class);
                    if (cfg != null) {
                        if (cfg.customItems == null) {
                            cfg.customItems = new ArrayList<>();
                        }
                        return cfg;
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            VaultWatcherClient.LOGGER.warn("[VaultWatcher] Не удалось загрузить конфиг, использую значения по умолчанию", e);
        }
        return new VaultWatcherConfig();
    }

    public void save() {
        if (configPath == null) {
            return;
        }
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            VaultWatcherClient.LOGGER.warn("[VaultWatcher] Не удалось сохранить конфиг", e);
        }
    }

    /** Добавляет кастомный id предмета (например "minecraft:trident"), без дублей. */
    public void addCustomItem(String id) {
        String normalized = id.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return;
        }
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>(customItems);
        set.add(normalized);
        customItems = new ArrayList<>(set);
        save();
    }

    public void removeCustomItem(String id) {
        customItems.remove(id);
        save();
    }
}
