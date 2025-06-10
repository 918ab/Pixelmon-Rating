package main.pixelmonrating.Command;

import main.pixelmonrating.Pixelmon_Rating;
import main.pixelmonrating.Util.DataManager;
import main.pixelmonrating.Util.GuiInventory;
import main.pixelmonrating.Util.Prefix;
import main.pixelmonrating.Util.SQLiteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class RatingCommand implements CommandExecutor {
    private final Pixelmon_Rating plugin;
    private final Map<UUID, DeleteRequest> deleteRequests = new HashMap<>();
    public RatingCommand(Pixelmon_Rating plugin){
        this.plugin = plugin;
    }
    private static class DeleteRequest {
        public final String ratingName;
        public final long timestamp;
        public DeleteRequest(String ratingName, long timestamp) {
            this.ratingName = ratingName;
            this.timestamp = timestamp;
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        String arg = args.length > 0 ? args[0] : "";
        DataManager data = new DataManager(plugin, "rating.yml");
        switch (arg) {
            case "생성": {
                if (!player.isOp()) {
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) != null) {
                    player.sendMessage(Prefix.PREFIX + "이미 존재하는 레이팅입니다");
                    break;
                }
                data.set("Rating." + ratingName + ".win.min", 10);
                data.set("Rating." + ratingName + ".win.max", 30);
                data.set("Rating." + ratingName + ".lose.min", -10);
                data.set("Rating." + ratingName + ".lose.max", -30);
                String scoreTableSQL = "CREATE TABLE IF NOT EXISTS `" + ratingName + "` (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "score INTEGER DEFAULT 0);";
                String dataTableSQL = "CREATE TABLE IF NOT EXISTS `" + ratingName + "_date` (" +
                        "uuid TEXT NOT NULL, " +
                        "result TEXT NOT NULL, " +
                        "score INTEGER NOT NULL, " +
                        "date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        ");";

                SQLiteManager db = new SQLiteManager(plugin, "data");
                db.createTable(scoreTableSQL);
                db.createTable(dataTableSQL);

                player.sendMessage(Prefix.PREFIX + "§a" + ratingName + "§f을(를) 생성했습니다");
                break;
            }
            case "삭제": {
                if (!player.isOp()) {
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }

                UUID playerId = player.getUniqueId();
                long now = System.currentTimeMillis();

                if (deleteRequests.containsKey(playerId)) {
                    DeleteRequest request = deleteRequests.get(playerId);
                    if (request.ratingName.equals(ratingName) && now - request.timestamp <= 5000) {
                        deleteRequests.remove(playerId);
                        data.remove("Rating." + ratingName);
                        SQLiteManager db = new SQLiteManager(plugin, "data");
                        db.executeSQL("DROP TABLE IF EXISTS `" + ratingName + "`;");
                        db.executeSQL("DROP TABLE IF EXISTS `" + ratingName + "_date`;");
                        player.sendMessage(Prefix.PREFIX + "§a" + ratingName + "§f을(를) 삭제했습니다");
                        break;
                    } else {
                        deleteRequests.put(playerId, new DeleteRequest(ratingName, now));
                        player.sendMessage(Prefix.PREFIX + "정말 삭제하시겠습니까?");
                        player.sendMessage(Prefix.PREFIX + "§c삭제시 §f" + ratingName + "§c의 관한 모든 데이터가 삭제됩니다");
                        player.sendMessage(Prefix.PREFIX + "삭제하시려면 5초안에 명령어를 다시 한번 입력해주세요");
                        break;
                    }
                } else {
                    deleteRequests.put(playerId, new DeleteRequest(ratingName, now));
                    player.sendMessage(Prefix.PREFIX + "정말 삭제하시겠습니까?");
                    player.sendMessage(Prefix.PREFIX + "§c삭제시 §f" + ratingName + "§c의 관한 모든 데이터가 삭제됩니다");
                    player.sendMessage(Prefix.PREFIX + "삭제하시려면 5초안에 명령어를 다시 한번 입력해주세요");
                    break;
                }
            }
            case "티어생성": {
                if (!player.isOp()) {
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "티어이름을 입력해주세요");
                    break;
                }
                if (data.get("Rating." + ratingName + ".tier." + args[2]) != null) {
                    player.sendMessage(Prefix.PREFIX + "이미 존재하는 티어입니다");
                    break;
                }
                data.set("Rating." + ratingName + ".tier." + args[2] + ".score", 10);
                player.sendMessage(Prefix.PREFIX + "§a" + args[2] + "§f 티어를 생성했습니다 (§a" + ratingName + "§f)");
                break;
            }
            case "티어삭제": {
                if (!player.isOp()) {
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "티어이름을 입력해주세요");
                    break;
                }
                if (data.get("Rating." + ratingName + ".tier." + args[2]) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 티어입니다");
                    break;
                }
                data.remove("Rating." + ratingName + ".tier." + args[2]);
                player.sendMessage(Prefix.PREFIX + "§a" + args[2] + "§f 티어를 삭제했습니다 (§a" + ratingName + "§f)");
                break;
            }
            case "설정":
                if (!player.isOp()) {
                    break;
                }
                new RatingSetting(plugin).handle(player, args);
                break;
            case "목록":
                if (!player.isOp()) {
                    break;
                }
                List<String> list = data.getKeys("Rating");
                if (list.isEmpty()) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이 존재하지 않습니다");
                    break;
                }
                for (String ratingName : list) {
                    player.sendMessage(" ");
                    player.sendMessage(Prefix.PREFIX + "§a[" + ratingName + "] ");
                    player.sendMessage(Prefix.PREFIX + "승리시 §a" + data.get("Rating." + ratingName + ".win.min") + "§f ~ §a" + data.get("Rating." + ratingName + ".win.max"));
                    player.sendMessage(Prefix.PREFIX + "패배시 §a" + data.get("Rating." + ratingName + ".lose.min") + "§f ~ §a" + data.get("Rating." + ratingName + ".lose.max"));
                    for (int i = 1; i <= 2; i++) {
                        String status = (data.get("Rating." + ratingName + ".battle-location." + i) != null) ? "§a설정됨" : "§c설정안됨";
                        player.sendMessage(Prefix.PREFIX + "배틀위치(" + i + ") " + status);
                    }

                    List<String> tiers = data.getKeys("Rating." + ratingName + ".tier");
                    if (tiers != null && !tiers.isEmpty()) {
                        Map<String, Integer> tierScores = new LinkedHashMap<>();
                        for (String tier : tiers) {
                            Integer score = Integer.parseInt(data.get("Rating." + ratingName + ".tier." + tier + ".score").toString());
                            if (score != null) {
                                tierScores.put(tier, score);
                            }
                        }
                        Map<Integer, Integer> scoreCount = new HashMap<>();
                        for (int score : tierScores.values()) {
                            scoreCount.put(score, scoreCount.getOrDefault(score, 0) + 1);
                        }
                        for (Map.Entry<String, Integer> entry : tierScores.entrySet()) {
                            String tier = entry.getKey();
                            int score = entry.getValue();
                            String msg = Prefix.PREFIX + tier + " §a" + score;
                            if (scoreCount.get(score) > 1) {
                                msg += " §e(⚠점수같음⚠)";
                            }
                            player.sendMessage(msg);
                        }
                    }else{
                        player.sendMessage(Prefix.PREFIX + "티어 §c설정안됨");
                    }
                    Boolean ma = isMatch(ratingName);
                    if (ma == true) {
                        player.sendMessage(Prefix.PREFIX + "§a매칭 가능");
                    } else {
                        player.sendMessage(Prefix.PREFIX + "§c매칭 불가능");
                    }
                }
                break;
            case "전적보기": {
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                GuiInventory.open_Record(plugin, player, ratingName);
                break;
            }
            case "순위": {
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                GuiInventory.open_Ranker(plugin, player, ratingName);
                break;
            }
            case "매칭": {
                if (args.length < 2) {
                    player.sendMessage(Prefix.PREFIX + "레이팅 이름을 입력해주세요");
                    break;
                }
                String ratingName = args[1];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (plugin.getMatchQueueManager().getEntry(player) != null) {
                    player.sendMessage(Prefix.PREFIX + "이미 매칭 대기열에 있습니다.");
                    break;
                }
                if (plugin.getMatchQueueManager().inMatchPlayers(player)) {
                    player.sendMessage(Prefix.PREFIX + "배틀중에는 이용할 수 없습니다");
                    return true;
                }
                Boolean ma = isMatch(ratingName);
                if (ma == false) {
                    player.sendMessage(Prefix.PREFIX + "§c⚠ 관리자에게 문의하세요 (설정오류)");
                    return true;
                }
                SQLiteManager db = new SQLiteManager(plugin, "data");
                Object result = db.select("SELECT score FROM `" + ratingName + "` WHERE uuid = ?", player.getUniqueId());
                int score = (result instanceof Number) ? ((Number) result).intValue() : 0;

                plugin.getMatchQueueManager().joinQueue(plugin, player, ratingName, score);
                plugin.getMatchQueueManager().showBossBar(plugin, player);
                break;
            }
            case "매칭취소": {
                if (plugin.getMatchQueueManager().getEntry(player) == null) {
                    player.sendMessage(Prefix.PREFIX + "매칭 대기열에 있지 않습니다");
                    break;
                }
                plugin.getMatchQueueManager().leaveQueue(player);
                player.sendMessage(Prefix.PREFIX + "매칭 대기열에서 제거되었습니다");
                plugin.getMatchQueueManager().hideBossBar(player);
                break;
            }
            default:
                if(!player.isOp()){
                    player.sendMessage(Prefix.PREFIX+"/레이팅 매칭 [레이팅이름]");
                    player.sendMessage(Prefix.PREFIX+"/레이팅 매칭취소");
                    player.sendMessage(Prefix.PREFIX+"/레이팅 순위 [레이팅이름]");
                    player.sendMessage(Prefix.PREFIX+"/레이팅 전적보기 [레이팅이름]");
                    break;
                }
                player.sendMessage(Prefix.PREFIX+"/레이팅 생성 [레이팅이름]"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 삭제 [레이팅이름]"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 티어생성 [레이팅이름] [티어이름]"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 티어삭제 [레이팅이름] [티어이름]"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 목록"); //
                player.sendMessage(Prefix.PREFIX+" ");
                player.sendMessage(Prefix.PREFIX+"/레이팅 매칭 [레이팅이름]");
                player.sendMessage(Prefix.PREFIX+"/레이팅 매칭취소");
                player.sendMessage(Prefix.PREFIX+"/레이팅 순위 [레이팅이름]"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 전적보기 [레이팅이름]"); //
                player.sendMessage(Prefix.PREFIX+" ");
                player.sendMessage(Prefix.PREFIX+"/레이팅 설정 변동요소 [레이팅이름] (승리,패배) (최소값) (최고값)"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 설정 티어점수값 [레이팅이름] [티어이름] (값)"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 설정 점수 [레이팅이름] [닉네임] (값)"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 설정 배틀위치 [레이팅이름] (1,2)"); //
                player.sendMessage(Prefix.PREFIX+"/레이팅 설정 최초보상 [레이팅이름]"); //
        }
        return true;
    }
    public boolean isMatch(String ratingName){
        DataManager data = new DataManager(plugin, "rating.yml");
        Boolean ma = true;
        for (int i = 1; i <= 2; i++) {
            String status = (data.get("Rating." + ratingName + ".battle-location." + i) != null) ? "§a설정됨" : "§c설정안됨";
            if (status.equals("§c설정안됨")) {
                ma = false;
            }
        }

        List<String> tiers = data.getKeys("Rating." + ratingName + ".tier");
        if (tiers != null && !tiers.isEmpty()) {
            Map<String, Integer> tierScores = new LinkedHashMap<>();
            for (String tier : tiers) {
                Integer score = Integer.parseInt(data.get("Rating." + ratingName + ".tier." + tier + ".score").toString());
                if (score != null) {
                    tierScores.put(tier, score);
                }
            }
            Map<Integer, Integer> scoreCount = new HashMap<>();
            for (int score : tierScores.values()) {
                scoreCount.put(score, scoreCount.getOrDefault(score, 0) + 1);
            }
            for (Map.Entry<String, Integer> entry : tierScores.entrySet()) {
                String tier = entry.getKey();
                int score = entry.getValue();
                String msg = Prefix.PREFIX + tier + " §a" + score;
                if (scoreCount.get(score) > 1) {
                    ma = false;
                }
            }
        } else {
            ma = false;
        }
        return ma;
    }
}

