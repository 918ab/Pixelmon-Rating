package main.pixelmonrating.Command;

import main.pixelmonrating.Pixelmon_Rating;
import main.pixelmonrating.Util.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RatingCommandTab implements TabCompleter {
    private final Pixelmon_Rating plugin;
    public  RatingCommandTab(Pixelmon_Rating plugin){
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();
        DataManager data = new DataManager(plugin, "rating.yml");
        String input = args[args.length-1].toLowerCase();
        if (args.length == 1) {
            candidates.add("매칭");
            candidates.add("매칭취소");
            candidates.add("순위");
            candidates.add("전적보기");
            if(sender.isOp()) {
                candidates.add("생성");
                candidates.add("삭제");
                candidates.add("티어생성");
                candidates.add("티어삭제");
                candidates.add("목록");
                candidates.add("설정");
            }
        }
        if (args.length == 2) {
            if(args[0].equals("순위") || args[0].equals("전적보기") || args[0].equals("매칭")){
                List<String> lists = data.getKeys("Rating");
                if(!lists.isEmpty()) {
                    candidates.addAll(lists);
                }else{
                    candidates.add("(존재하지않음)");
                }
            }
            if(sender.isOp()) {
                if (args[0].equals("설정")) {
                    candidates.add("변동요소");
                    candidates.add("티어점수값");
                    candidates.add("점수");
                    candidates.add("배틀위치");
                    candidates.add("최초보상");
                }
                if (args[0].equals("생성")){
                    candidates.add("(레이팅이름)");
                }
                if (args[0].equals("삭제") || args[0].equals("티어생성") || args[0].equals("티어삭제")) {
                    List<String> lists = data.getKeys("Rating");
                    if(!lists.isEmpty()) {
                        candidates.addAll(lists);
                    }else{
                        candidates.add("(존재하지않음)");
                    }
                }
            }
        }
        if (args.length == 3) {
            if (args[0].equals("티어생성")) {
                candidates.add("(티어이름)");
            }
            if (args[0].equals("티어삭제")) {
                List<String> lists = data.getKeys("Rating."+args[1]+".tier");
                if(!lists.isEmpty()) {
                    candidates.addAll(lists);
                }else{
                    candidates.add("(존재하지않음)");
                }
            }
            if (args[0].equals("설정")) {
                List<String> lists = data.getKeys("Rating");
                if(!lists.isEmpty()) {
                    candidates.addAll(lists);
                }else{
                    candidates.add("(존재하지않음)");
                }
            }
        }
        if (args.length == 4) {
            if (args[1].equals("변동요소")) {
                candidates.add("승리");
                candidates.add("패배");
            }
            if (args[1].equals("배틀위치")) {
                candidates.add("1");
                candidates.add("2");
            }
            if (args[1].equals("티어점수값") || args[1].equals("최초보상")) {
                List<String> lists = data.getKeys("Rating."+args[2]+".tier");
                if(!lists.isEmpty()) {
                    candidates.addAll(lists);
                }else{
                    candidates.add("(존재하지않음)");
                }
            }
            if(args[1].equals("점수")){
                for(Player player : Bukkit.getOnlinePlayers()){
                    candidates.add(player.getName());
                }
            }
        }
        if (args.length == 5) {
            if (args[1].equals("변동요소")) {
                candidates.add("(최소값)");
            }
            if (args[1].equals("티어점수값") || args[1].equals("점수")) {
                candidates.add("(값)");
            }
        }
        if (args.length == 6) {
            if (args[1].equals("변동요소")) {
                candidates.add("(최고값)");
            }
        }
        for (String candidate : candidates) {
            if (candidate.toLowerCase().contains(input)) {
                completions.add(candidate);
            }
        }
        return completions;
    }
}
