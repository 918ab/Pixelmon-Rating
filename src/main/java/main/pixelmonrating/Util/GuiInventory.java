package main.pixelmonrating.Util;

import main.pixelmonrating.Pixelmon_Rating;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GuiInventory {

    public static void open_Record(Pixelmon_Rating plugin, Player player, String ratingName) {
        SQLiteManager db = new SQLiteManager(plugin, "data");
        Inventory inv = Bukkit.createInventory(null, 27, Prefix.PREFIX + "전적 (" + ratingName + ")");

        inv.setItem(18, new ItemBuilder(Material.ARROW)
                .setDisplayName("§a랭크 보러가기")
                .addLore(" §7- " + ratingName)
                .build());

        UUID uuid = player.getUniqueId();
        List<Object[]> records = db.selects(
                "SELECT result, score, date FROM `" + ratingName + "_date` WHERE uuid = ? ORDER BY date DESC LIMIT 9",
                uuid
        );

        setPlayerStatsHead(plugin, inv, db, player, ratingName, 26);

        int slot = 17;
        for (Object[] row : records) {
            String result = row[0].toString();
            int score = ((Number) row[1]).intValue();
            String date = row[2].toString();

            Material icon;
            switch (result) {
                case "win":
                    icon = Material.LIME_WOOL;
                    break;
                case "lose":
                    icon = Material.RED_WOOL;
                    break;
                default:
                    icon = Material.BARRIER;
                    break;
            }

            String resultString;
            switch (result) {
                case "win":
                    resultString = "승리";
                    break;
                case "lose":
                    resultString = "패배";
                    break;
                default:
                    resultString = "관리자설정";
                    break;
            }

            inv.setItem(slot--, new ItemBuilder(icon)
                    .setDisplayName("§f" + ratingName)
                    .addLore(" ")
                    .addLore(" §e승부 : §f" + resultString)
                    .addLore(" §a점수 : §f" + score)
                    .addLore(" §b시간 : §f" + date)
                    .build());
        }

        player.openInventory(inv);
    }

    public static void open_Ranker(Pixelmon_Rating plugin, Player player, String ratingName) {
        SQLiteManager db = new SQLiteManager(plugin, "data");
        Inventory inv = Bukkit.createInventory(null, 36, Prefix.PREFIX + "순위 (" + ratingName + ")");

        inv.setItem(27, new ItemBuilder(Material.ARROW)
                .setDisplayName("§a자신의 전적 보러가기")
                .addLore(" §7- " + ratingName)
                .build());

        setPlayerStatsHead(plugin, inv, db, player, ratingName, 35);

        List<Object[]> tops = db.selects("SELECT uuid, score FROM `" + ratingName + "` ORDER BY score DESC LIMIT 10");
        List<Integer> slots = Arrays.asList(4, 12, 13, 14, 20, 21, 22, 23, 24);
        DataManager data = new DataManager(plugin, "rating.yml");
        for (int i = 0; i < tops.size() && i < slots.size(); i++) {
            Object[] row = tops.get(i);
            String uuidStr = row[0].toString();
            int score = ((Number) row[1]).intValue();
            OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));

            int win = getCount(db, ratingName + "_date", uuidStr, "win");
            int lose = getCount(db, ratingName + "_date", uuidStr, "lose");

            String tier = plugin.getMatchQueueManager().getPlayerTier(data,ratingName,score);
            inv.setItem(slots.get(i), new ItemBuilder(Material.PLAYER_HEAD)
                    .setSkullOwner(topPlayer)
                    .setDisplayName("§f" + topPlayer.getName() + " §e(" + (i + 1) + "등)")
                    .addLore(" ")
                    .addLore(" §b티어 : §f"+tier)
                    .addLore(" ")
                    .addLore(" §e점수 : §f" + score)
                    .addLore(" §a승리 : §f" + win)
                    .addLore(" §c패배 : §f" + lose)
                    .addLore(" ")
                    .build());
        }

        player.openInventory(inv);
    }

    private static void setPlayerStatsHead(Pixelmon_Rating plugin, Inventory inv, SQLiteManager db, Player player, String ratingName, int slot) {
        UUID uuid = player.getUniqueId();
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);

        int rank = 0, score = 0, win = 0, lose = 0;
        Object rankObj = db.select("SELECT rank FROM (SELECT uuid, RANK() OVER (ORDER BY score DESC) AS rank FROM `" + ratingName + "`) WHERE uuid = ?", uuid);
        if (rankObj instanceof Number) rank = ((Number) rankObj).intValue();

        Object scoreObj = db.select("SELECT score FROM `" + ratingName + "` WHERE uuid = ?", uuid);
        if (scoreObj instanceof Number) score = ((Number) scoreObj).intValue();

        win = getCount(db, ratingName + "_date", uuid.toString(), "win");
        lose = getCount(db, ratingName + "_date", uuid.toString(), "lose");
        DataManager data = new DataManager(plugin, "rating.yml");
        String tier = plugin.getMatchQueueManager().getPlayerTier(data,ratingName,score);
        inv.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullOwner(op)
                .setDisplayName("§f" + player.getName() + " §e(" + (rank) + "등)")
                .addLore(" ")
                .addLore(" §b티어 : §f"+tier)
                .addLore(" ")
                .addLore(" §e점수 : §f" + score)
                .addLore(" §a승리 : §f" + win)
                .addLore(" §c패배 : §f" + lose)
                .addLore(" ")
                .build());
    }

    private static int getCount(SQLiteManager db, String table, String uuid, String result) {
        Object obj = db.select("SELECT COUNT(*) FROM `" + table + "` WHERE uuid = ? AND result = ?", uuid, result);
        return (obj instanceof Number) ? ((Number) obj).intValue() : 0;
    }

    public static void giveTier(Pixelmon_Rating plugin, Player player, String ratingName, String tier){
        if(tier.equals("없음")) { return;}
        SQLiteManager db = new SQLiteManager(plugin, "data");

        Object exist = db.select("SELECT uuid FROM use_table WHERE uuid = ? AND rating_name = ? AND tier = ?",
                player.getUniqueId(), ratingName, tier
        );
        if (exist == null) {
            DataManager data = new DataManager(plugin, "rating.yml");
            List<String> tiers = data.getKeys("Rating."+ratingName+".tier."+tier);
            tiers.remove("score");
            if(tiers.isEmpty()) { return; }
            boolean clear = false;
            for(String ti : tiers){
                ItemStack item = (ItemStack) data.get("Rating."+ratingName+".tier."+tier+"."+ti);
                if(item != null) {
                    player.getInventory().addItem(item);
                    clear = true;
                }
            }
            if(clear == true){
                db.executeSQL("INSERT INTO use_table (uuid, rating_name, tier) VALUES (?, ?, ?)", player.getUniqueId(), ratingName, tier);
            }
            player.sendMessage(Prefix.PREFIX + "§a"+tier+"§f보상을 지급 받았습니다! ("+ratingName+")");
        }
    }
}