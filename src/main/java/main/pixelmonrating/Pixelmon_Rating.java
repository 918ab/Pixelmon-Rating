package main.pixelmonrating;


import main.pixelmonrating.Command.RatingCommand;
import main.pixelmonrating.Command.RatingCommandTab;
import main.pixelmonrating.Event.PixelMonEvent;
import main.pixelmonrating.Event.RatingEvent;
import main.pixelmonrating.Match.MatchQueueManager;
import main.pixelmonrating.Util.DataManager;
import main.pixelmonrating.Util.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;


public final class Pixelmon_Rating extends JavaPlugin {
    private PixelMonEvent pixelE = new PixelMonEvent();
    private MatchQueueManager matchQueueManager;
    @Override
    public void onEnable() {
        ModRegister();
        matchQueueManager = new MatchQueueManager();

        getLogger().info("[Pixelmon_Rating] Plugin Enable");
        getCommand("레이팅").setExecutor(new RatingCommand(this));
        getCommand("레이팅").setTabCompleter(new RatingCommandTab(this));


        getServer().getPluginManager().registerEvents(new RatingEvent(this), this);
        DataManager data = new DataManager(this, "rating.yml");
        data.saveResourceIfNotExists(this);
        String firstClear = "CREATE TABLE IF NOT EXISTS `" + "use_table` (" +
                "uuid TEXT NOT NULL, " +
                "rating_name TEXT NOT NULL, " +
                "tier TEXT NOT NULL, " +
                "date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String location = "CREATE TABLE IF NOT EXISTS `location_table` (" +
                "uuid TEXT NOT NULL, " +
                "rating_name TEXT NOT NULL, " +
                "loc TEXT NOT NULL" +
                ");";

        SQLiteManager db = new SQLiteManager(this, "data");
        db.createTable(firstClear);
        db.createTable(location);
    }


    @Override
    public void onDisable() {
        SQLiteManager db = new SQLiteManager(this, "data");
        db.close();
    }


    private void ModRegister() {
        Bukkit.getServicesManager().register(PixelMonEvent.class, new PixelMonEvent(), this, ServicePriority.Normal);
        Bukkit.getPluginManager().registerEvents(new PixelMonEvent(), this);
        this.pixelE.register(this);
    }

    public MatchQueueManager getMatchQueueManager() {
        return matchQueueManager;
    }
}
