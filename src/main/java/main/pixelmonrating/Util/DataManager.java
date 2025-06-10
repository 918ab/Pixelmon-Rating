package main.pixelmonrating.util;

import main.pixelmonrating.Pixelmon_Rating;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigGet {
    private final Pixelmon_Rating plugin;

    public ConfigGet(Pixelmon_Rating plugin) {
        this.plugin = plugin;
    }
    public void setConfig(String text,Object value) {
        FileConfiguration config = plugin.getConfig();
        config.set(text,value);
        plugin.saveConfig();
    }
    public Object get(String key) {
        FileConfiguration config = plugin.getConfig();
        return config.get(key);
    }
    public List<String> getNames(String key) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection Section = config.getConfigurationSection(key);
        if (Section != null) {
            Set<String> keys = Section.getKeys(false);
            return new ArrayList<>(keys);
        }
        return new ArrayList<>();
    }


}
