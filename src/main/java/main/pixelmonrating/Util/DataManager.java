package main.pixelmonrating.Util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataManager {
    private final File configFile;
    private final FileConfiguration config;

    public DataManager(Plugin plugin, String fileName) {
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void set(String key, Object value) {
        config.set(key, value);
        save();
    }

    public Object get(String key) {
        return config.get(key);
    }

    public void remove(String key) {
        config.set(key, null);
        save();
    }

    public List<String> getKeys(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            return new ArrayList<>(keys);
        }
        return new ArrayList<>();
    }

    private void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveResourceIfNotExists(Plugin plugin) {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try (InputStream in = plugin.getResource(configFile.getName())) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    Bukkit.getLogger().info(configFile.getName() + " 리소스를 찾을 수 없습니다.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}