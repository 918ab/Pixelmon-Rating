package main.pixelmonrating.Match;

import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import main.pixelmonrating.Pixelmon_Rating;
import main.pixelmonrating.Util.DataManager;
import main.pixelmonrating.Util.Prefix;
import main.pixelmonrating.Util.SQLiteManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchQueueManager {
    private static final int MAX_SCORE_DIFF = 200;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, BukkitTask> bossBarTasks = new HashMap<>();
    private final Map<String, Boolean> arenaOccupied = new HashMap<>();
    private final Set<UUID> inMatchPlayers = new HashSet<>();

    private final Queue<MatchEntry> queue = new ConcurrentLinkedQueue<>();

    public void joinQueue(Pixelmon_Rating plugin, Player player, String ratingName, int score) {
        MatchEntry entry = new MatchEntry(player, ratingName, score);
        queue.add(entry);
        new BukkitRunnable() {
            @Override
            public void run() {
                DataManager data = new DataManager(plugin, "rating.yml");
                SQLiteManager db = new SQLiteManager(plugin, "data");
                plugin.getMatchQueueManager().processMatching(plugin, data, db);
            }
        }.runTaskLater(plugin, 60);
    }
    public void showBossBar(Pixelmon_Rating plugin, Player player) {
        MatchEntry entry = getEntry(player);
        if (entry == null) return;

        UUID uuid = player.getUniqueId();
        BossBar bar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SEGMENTED_10);
        bar.addPlayer(player);
        bar.setProgress(1.0);
        bossBars.put(uuid, bar);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            MatchEntry currentEntry = getEntry(player);
            if (currentEntry == null) return;

            int position = getPosition(player);
            String ratingName = currentEntry.getRatingName();
            long elapsedMillis = System.currentTimeMillis() - currentEntry.getJoinTime();

            long seconds = elapsedMillis / 1000;
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, secs);

            bar.setTitle("§e[매칭중] §f레이팅: " + ratingName + " | 대기 순위: " + position + "위 | 대기 시간: " + timeFormatted);
        }, 0L, 20L);

        bossBarTasks.put(uuid, task);
    }
    public void leaveQueue(Player player) {
        queue.removeIf(e -> e.getPlayer().equals(player));
    }
    public void hideBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();
    }
    public void processMatching(Pixelmon_Rating plugin, DataManager data, SQLiteManager db) {
        List<MatchEntry> sortedQueue = new ArrayList<>(queue);
        sortedQueue.sort(Comparator.comparingLong(MatchEntry::getJoinTime));

        for (int i = 0; i < sortedQueue.size(); i++) {
            MatchEntry entry1 = sortedQueue.get(i);
            if (!isArenaAvailable(entry1.getRatingName())) {
                continue;
            }

            MatchEntry bestMatch = null;
            int minDiff = Integer.MAX_VALUE;

            for (int j = i + 1; j < sortedQueue.size(); j++) {
                MatchEntry entry2 = sortedQueue.get(j);

                if (!entry1.getRatingName().equals(entry2.getRatingName())) continue;

                int diff = Math.abs(entry1.getRatingScore() - entry2.getRatingScore());
                if (diff <= MAX_SCORE_DIFF && diff < minDiff) {
                    bestMatch = entry2;
                    minDiff = diff;
                }
            }

            if (bestMatch != null) {
                occupyArena(entry1.getRatingName());
                queue.remove(entry1);
                queue.remove(bestMatch);
                Player p1 = entry1.getPlayer();
                Player p2 = bestMatch.getPlayer();
                String ratingName = entry1.getRatingName();
                saveLocation(db, p1, ratingName, p1.getLocation());
                saveLocation(db, p2, ratingName, p2.getLocation());

                inMatchPlayers.add(p1.getUniqueId());
                inMatchPlayers.add(p2.getUniqueId());
                hideBossBar(p1);
                hideBossBar(p2);

                playerInfo(data,db, p1,p2,ratingName);
                playerInfo(data,db, p2,p1,ratingName);

                new BukkitRunnable() {
                    int count = 10;

                    @Override
                    public void run() {
                        if (count > 0) {
                            p1.sendTitle("§a매칭성공!","§b"+count + "§f초 후 이동합니다",0,25,0);
                            p2.sendTitle("§a매칭성공!","§b"+count + "§f초 후 이동합니다",0,25,0);

                            p1.playSound(p1.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
                            p2.playSound(p2.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
                            count--;
                        } else {
                            cancel();
                            Location loc1 = (Location) data.get("Rating." + ratingName + ".battle-location.1");
                            Location loc2 = (Location) data.get("Rating." + ratingName + ".battle-location.2");

                            p1.teleport(loc1);
                            p2.teleport(loc2);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pokeheal "+p1.getName());
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pokeheal "+p2.getName());
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pokebattle "+p1.getName()+" "+p2.getName());
                                }
                            }.runTaskLater(plugin, 100L);
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        }
    }

    public int getPosition(Player player) {
        MatchEntry entry = getEntry(player);
        if (entry == null) return -1;

        String ratingName = entry.getRatingName();
        int index = 1;

        for (MatchEntry e : queue) {
            if (e.getRatingName().equals(ratingName)) {
                if (e.getPlayer().equals(player)) {
                    return index;
                }
                index++;
            }
        }

        return -1;
    }

    public MatchEntry getEntry(Player player) {
        for (MatchEntry entry : queue) {
            if (entry.getPlayer().equals(player)) return entry;
        }
        return null;
    }

    public boolean isArenaAvailable(String ratingName) {
        return !arenaOccupied.getOrDefault(ratingName, false);
    }

    public void occupyArena(String ratingName) {
        arenaOccupied.put(ratingName, true);
    }


    public void releaseArena(SQLiteManager db, String ratingName) {
        List<Object[]> results = db.selects("SELECT uuid, loc FROM location_table WHERE rating_name = ?", ratingName);
        for (Object[] row : results) {
            String uuidStr = row[0].toString();
            String locStr = row[1].toString();
            UUID uuid = UUID.fromString(uuidStr);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            Location loc = deserializeLocation(locStr);
            if (loc != null) {
                player.teleport(loc);
            }

            inMatchPlayers.remove(uuid);
        }
        db.executeSQL("DELETE FROM location_table WHERE rating_name = ?", ratingName);
        arenaOccupied.put(ratingName, false);
    }
    private void saveLocation(SQLiteManager db, Player player, String ratingName, Location loc) {
        String serialized = serializeLocation(loc);
        db.insert("INSERT INTO location_table (uuid, rating_name, loc) VALUES (?, ?, ?)",
                player.getUniqueId().toString(), ratingName, serialized);
    }

    public static String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public static Location deserializeLocation(String s) {
        String[] parts = s.split(",");
        if (parts.length < 6) return null;
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
    }


    public boolean inMatchPlayers(Player player) {
        return inMatchPlayers.contains(player.getUniqueId());
    }

    public void playerInfo(DataManager data, SQLiteManager db, Player player, Player target,String ratingName) {
        String uuid = target.getUniqueId().toString();

        List<Object[]> result = db.selects("SELECT score FROM `" + ratingName + "` WHERE uuid = ?", uuid);
        int score = 0;
        if (!result.isEmpty()) {
            Object val = result.get(0)[0];
            if (val instanceof Number) {
                score = ((Number) val).intValue();
            }
        }

        int win = getCount(db, ratingName, uuid, "win");
        int lose = getCount(db, ratingName, uuid, "lose");
        String tier = getPlayerTier(data, ratingName, score);

        player.sendMessage(" ");
        player.sendMessage(Prefix.PREFIX + target.getName()+"님의 정보");
        player.sendMessage(Prefix.PREFIX + " §b티어 §f: "+tier);
        player.sendMessage(Prefix.PREFIX + " §a점수 §f: "+score);
        player.sendMessage(Prefix.PREFIX + " §2승리 §f: "+win);
        player.sendMessage(Prefix.PREFIX + " §4패배 §f: "+lose);
        player.sendMessage(" ");
    }
    public int getPokedexCount(ServerPlayerEntity player) {
        PlayerPartyStorage storage = StorageProxy.getParty(player);
        return storage.playerPokedex.countSeen();
    }
    public String getPlayerTier(DataManager data, String ratingName, int score) {
        List<String> tierSection = data.getKeys("Rating." + ratingName + ".tier");

        String selectedTier = "없음";
        int maxMatchedScore = 0;

        for (String tier : tierSection ) {
            int tierScore = (int) data.get("Rating." + ratingName + ".tier." + tier + ".score");
            if (score >= tierScore && tierScore > maxMatchedScore) {
                selectedTier = tier;
                maxMatchedScore = tierScore;
            }
        }
        return selectedTier;
    }

    public static int getCount(SQLiteManager db, String ratingName, String uuid, String resultType) {
        List<Object[]> result = db.selects(
                "SELECT COUNT(*) FROM `" + ratingName + "_date` WHERE uuid = ? AND result = ?",
                uuid, resultType
        );
        if (!result.isEmpty()) {
            Object val = result.get(0)[0];
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
        }
        return 0;
    }

}