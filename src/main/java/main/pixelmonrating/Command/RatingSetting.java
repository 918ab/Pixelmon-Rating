package main.pixelmonrating.Command;

import main.pixelmonrating.Pixelmon_Rating;
import main.pixelmonrating.Util.DataManager;
import main.pixelmonrating.Util.GuiInventory;
import main.pixelmonrating.Util.Prefix;
import main.pixelmonrating.Util.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RatingSetting {
    private final Pixelmon_Rating plugin;
    public RatingSetting(Pixelmon_Rating plugin){
        this.plugin = plugin;
    }
    public void handle(Player player, String[] args) {
        String arg = args.length > 1 ? args[1] : "";
        DataManager data = new DataManager(plugin, "rating.yml");
        switch (arg) {
            case "변동요소": {
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[2];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 4) {
                    player.sendMessage(Prefix.PREFIX + "승리 혹은 패배를 입력해주세요");
                    break;
                }
                if (args.length < 5) {
                    player.sendMessage(Prefix.PREFIX + "최소점수를 입력해주세요");
                    break;
                }
                if (args.length < 6) {
                    player.sendMessage(Prefix.PREFIX + "최대점수를 입력해주세요");
                    break;
                }

                int min, max;
                try {
                    min = Integer.parseInt(args[4]);
                    max = Integer.parseInt(args[5]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Prefix.PREFIX + "정수가 아닙니다");
                    break;
                }
                if (args[3].equals("승리")) {
                    data.set("Rating." + ratingName + ".win.min", min);
                    data.set("Rating." + ratingName + ".win.max", max);
                } else if (args[3].equals("패배")) {
                    data.set("Rating." + ratingName + ".lose.min", min);
                    data.set("Rating." + ratingName + ".lose.max", max);
                } else {
                    player.sendMessage(Prefix.PREFIX + "승리 혹은 패배를 입력해주세요");
                    break;
                }

                player.sendMessage(Prefix.PREFIX +"§a"+ ratingName + "§f의 §a" + args[3] + "§f시 점수변동요소를 §a" + args[4] + "§f ~ §a" + args[5] + "§f로 설정했습니다");
                break;
            }
            case "티어점수값": {
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[2];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 4) {
                    player.sendMessage(Prefix.PREFIX + "티어이름을 입력해주세요");
                    break;
                }
                if (data.get("Rating." + ratingName + ".tier." + args[3]) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 티어입니다");
                    break;
                }
                if (args.length < 5) {
                    player.sendMessage(Prefix.PREFIX + "값을 입력해주세요");
                    break;
                }
                int value;
                try {
                    value = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Prefix.PREFIX + "정수가 아닙니다");
                    break;
                }
                data.set("Rating." + ratingName + ".tier." + args[3] +".score", value);
                player.sendMessage(Prefix.PREFIX +"§a"+ ratingName + "§f의 §a" + args[3] + "§f 티어의 점수를 §a" + value + "§f로 설정했습니다");
                break;
            }
            case "점수": {
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[2];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 4) {
                    player.sendMessage(Prefix.PREFIX + "닉네임을 입력해주세요");
                    break;
                }
                Player target = Bukkit.getPlayer(args[3]);
                if(target == null){
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 플레이어입니다");
                    return;
                }
                if (args.length < 5) {
                    player.sendMessage(Prefix.PREFIX + "값을 입력해주세요");
                    break;
                }
                int value;
                try {
                    value = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Prefix.PREFIX + "정수가 아닙니다");
                    break;
                }
                SQLiteManager db = new SQLiteManager(plugin, "data");

                String insertDateSQL = "INSERT INTO `" + ratingName + "_date` (uuid, result, score) VALUES (?, ?, ?)";
                String insertSQL = "INSERT OR REPLACE INTO `" + ratingName + "` (uuid, score) VALUES (?, ?)";
                Object result = db.select("SELECT score FROM `" + ratingName + "` WHERE uuid = ?", target.getUniqueId());

                db.insert(insertDateSQL, target.getUniqueId().toString(), "forced", value);
                db.insert(insertSQL, target.getUniqueId().toString(), value);
                player.sendMessage(Prefix.PREFIX +"§a"+ target.getName()+"§f("+target.getUniqueId()+")");
                String tier = plugin.getMatchQueueManager().getPlayerTier(data,ratingName,value);
                GuiInventory.giveTier(plugin,target,ratingName,tier);
                if(result != null){
                    player.sendMessage(Prefix.PREFIX +"§7"+result+" §f-> §a"+value+" §f설정 완료");
                }else{
                    player.sendMessage(Prefix.PREFIX +"§a"+value+" §f설정 완료");
                }
                break;
            }
            case "배틀위치": {
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[2];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 4) {
                    player.sendMessage(Prefix.PREFIX + "1 혹은 2를 입력해주세요");
                    break;
                }
                if (args[3].equals("1")) {
                    data.set("Rating." + ratingName+".battle-location.1",player.getLocation());
                } else if (args[3].equals("2")) {
                    data.set("Rating." + ratingName+".battle-location.2",player.getLocation());
                } else {
                    player.sendMessage(Prefix.PREFIX + "1 혹은 2를 입력해주세요");
                    break;
                }
                player.sendMessage(Prefix.PREFIX +"§a"+ ratingName + "§f의 배틀위치를 현재위치로 설정했습니다 (§a"+args[3]+"§f)");
                break;
            }
            case "최초보상":
                if (args.length < 3) {
                    player.sendMessage(Prefix.PREFIX + "레이팅이름을 입력해주세요");
                    break;
                }
                String ratingName = args[2];
                if (data.get("Rating." + ratingName) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 레이팅입니다");
                    break;
                }
                if (args.length < 4) {
                    player.sendMessage(Prefix.PREFIX + "티어이름을 입력해주세요");
                    break;
                }
                if (data.get("Rating." + ratingName + ".tier." + args[3]) == null) {
                    player.sendMessage(Prefix.PREFIX + "존재하지 않는 티어입니다");
                    break;
                }
                Inventory inv = Bukkit.createInventory(null, 54, Prefix.PREFIX+"최초보상/"+args[2]+"/"+args[3]);
                for (int i = 0; i < 54; i++) {
                    ItemStack item = (ItemStack) data.get("Rating."+args[2]+".tier."+args[3]+"."+i);
                    if(item != null){
                        inv.setItem(i,item);
                    }
                }
                player.openInventory(inv);
                break;
            default:
                player.sendMessage(Prefix.PREFIX+" 변동요소, 티어점수값, 점수, 배틀위치, 최초보상 중에 입력해주세요");
        }
    }
}
