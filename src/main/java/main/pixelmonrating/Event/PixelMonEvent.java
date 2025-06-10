package main.pixelmonrating.Event;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import main.pixelmonrating.Pixelmon_Rating;
import main.pixelmonrating.Util.DataManager;
import main.pixelmonrating.Util.GuiInventory;
import main.pixelmonrating.Util.Prefix;
import main.pixelmonrating.Util.SQLiteManager;
import net.minecraftforge.eventbus.api.EventPriority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class PixelMonEvent implements Listener {
    private Pixelmon_Rating plugin;

    public final void register(Pixelmon_Rating plugin) {
        this.plugin = plugin;
        Pixelmon.EVENT_BUS.register(this);
        Pixelmon.EVENT_BUS.addListener(EventPriority.NORMAL, true, BattleEndEvent.class, this::BattleEndEvent);
    }

    public void BattleEndEvent(BattleEndEvent event) {
        Map<BattleParticipant, BattleResults> resultMap = event.getResults();
        if (resultMap == null) return;
        Map<Player, BattleResults> list = new HashMap<>();

        for (Map.Entry<BattleParticipant, BattleResults> entry : resultMap.entrySet()) {
            BattleParticipant participant = entry.getKey();
            BattleResults result = entry.getValue();

            String name = participant.getDisplayName();
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                if (plugin.getMatchQueueManager().inMatchPlayers(player) == true) {
                    list.put(player, result);
                } else {
                    return;
                }
            }else {
                return;
            }
        }
        if(list.isEmpty() ){ return; }
        String ratingName = "";
        SQLiteManager db = new SQLiteManager(plugin, "data");
        for (Map.Entry<Player, BattleResults> maps : list.entrySet()) {
            Player player = maps.getKey();
            BattleResults result = maps.getValue();
            DataManager data = new DataManager(plugin, "rating.yml");
            Object obj = db.select("SELECT rating_name FROM location_table WHERE uuid = ?", player.getUniqueId());
            ratingName = (obj != null) ? obj.toString() : null;
            Object objScore = db.select("SELECT score FROM `" + ratingName + "` WHERE uuid = ?", player.getUniqueId());
            int currentScore = (objScore instanceof Number) ? ((Number) objScore).intValue() : 0;
            if(result == BattleResults.VICTORY){
                int min = (int) data.get("Rating."+ratingName+".win.min");
                int max = (int) data.get("Rating."+ratingName+".win.max");
                int score = getRandom(min,max);
                currentScore = currentScore + score;
                player.sendMessage(Prefix.PREFIX + " ");
                player.sendMessage(Prefix.PREFIX + "당신이 승리하였습니다");
                player.sendMessage(Prefix.PREFIX + "§e+"+score);
                player.sendMessage(Prefix.PREFIX + " ");

            }if(result == BattleResults.DEFEAT){
                int min = (int) data.get("Rating."+ratingName+".lose.min");
                int max = (int) data.get("Rating."+ratingName+".lose.max");
                int score = getRandom(min,max);
                currentScore = currentScore + score;
                if(currentScore < 0){
                    currentScore = 0;
                }
                player.sendMessage(Prefix.PREFIX + " ");
                player.sendMessage(Prefix.PREFIX + "당신이 패배하였습니다");
                player.sendMessage(Prefix.PREFIX + "§c"+score);
                player.sendMessage(Prefix.PREFIX + " ");
            }
            setScore(db,ratingName,currentScore,player,result);
            player.sendMessage(Prefix.PREFIX + "10초후 원래 위치로 복귀합니다!");

        }
        String finalRatingName = ratingName;
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getMatchQueueManager().releaseArena(db, finalRatingName);
            }
        }.runTaskLater(plugin, 200L);

    }
    public void setScore(SQLiteManager db,String ratingName, int currentScore, Player player,BattleResults result){
        Object exist = db.select("SELECT uuid FROM `" + ratingName + "` WHERE uuid = ?", player.getUniqueId());
        if (exist != null) {
            db.executeSQL("UPDATE `" + ratingName + "` SET score = ? WHERE uuid = ?", currentScore, player.getUniqueId());
        } else {
            db.executeSQL("INSERT INTO `" + ratingName + "` (uuid, score) VALUES (?, ?)", player.getUniqueId(), currentScore);
        }
        db.executeSQL("INSERT INTO `" + ratingName + "_date` (uuid, result, score) VALUES (?, ?, ?)",
                player.getUniqueId().toString(),
                (result == BattleResults.VICTORY) ? "win" : "lose",
                currentScore);
        DataManager data = new DataManager(plugin, "rating.yml");
        String tier = plugin.getMatchQueueManager().getPlayerTier(data,ratingName,currentScore);
        List<String> lists = data.getKeys("Rating."+ratingName+".tier");
        for(String list : lists){
            if(tier.equals(list)){
                GuiInventory.giveTier(plugin,player,ratingName,tier);
            }
        }
    }

    public int getRandom(int a, int b) {
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        return new Random().nextInt(max - min + 1) + min;
    }
}
