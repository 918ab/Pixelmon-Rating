package main.pixelmonrating.Match;
import org.bukkit.entity.Player;

public class MatchEntry {
    private final Player player;
    private final String ratingName;
    private final int ratingScore;
    private final long joinTime;

    public MatchEntry(Player player, String ratingName, int ratingScore) {
        this.player = player;
        this.ratingName = ratingName;
        this.ratingScore = ratingScore;
        this.joinTime = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public String getRatingName() {
        return ratingName;
    }

    public int getRatingScore() {
        return ratingScore;
    }

    public long getJoinTime() {
        return joinTime;
    }
}