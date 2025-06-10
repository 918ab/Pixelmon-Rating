package main.pixelmonrating;

import main.pixelmonrating.Command.RatingCommand;
import main.pixelmonrating.Command.RatingCommandTab;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Pixelmo_Rating extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("[Pixelmon_Rating]");
        getCommand("레이팅").setExecutor(new RatingCommand());
        getCommand("레이팅").setTabCompleter(new RatingCommandTab());
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
