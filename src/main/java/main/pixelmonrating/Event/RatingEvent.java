package main.pixelmonrating.Event;



import main.pixelmonrating.Pixelmon_Rating;
import main.pixelmonrating.Util.DataManager;
import main.pixelmonrating.Util.GuiInventory;
import main.pixelmonrating.Util.Prefix;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RatingEvent implements Listener {
    private final Pixelmon_Rating plugin;
    public RatingEvent(Pixelmon_Rating plugin) {
        this.plugin = plugin;

    }
    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent e){
        if (e.getView().getTitle().contains(Prefix.PREFIX + "최초보상/")) {
            Player player = (Player) e.getPlayer();
            DataManager data = new DataManager(plugin, "rating.yml");
            String[] list = e.getView().getTitle().split("/");
            Boolean use = false;
            for (int i = 0; i < 54; i++) {
                ItemStack item = e.getInventory().getItem(i);
                String path = "Rating." + list[1] + ".tier." + list[2] + "." + i;
                ItemStack savedItem = (ItemStack) data.get(path);
                if (item != null) {
                    if (savedItem == null || !item.equals(savedItem)) {
                        data.set(path, item);
                        use = true;
                    }
                } else {
                    if (savedItem != null) {
                        data.remove(path);
                        use = true;
                    }
                }
            }
            if (use == true){
                player.sendMessage(Prefix.PREFIX + "최초보상을 설정했습니다(" + list[1] + ")(" + list[2] + ")");
            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e){
        if (e.getView().getTitle().contains(Prefix.PREFIX + "전적 (")) {
            e.setCancelled(true);

            if (e.getSlot() == 18) {
                ItemStack item = e.getCurrentItem();
                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

                List<String> lore = item.getItemMeta().getLore();

                String ratingName = ChatColor.stripColor(lore.get(0));
                ratingName = ratingName.replace(" - ","");
                Player player = (Player) e.getWhoClicked();
                GuiInventory.open_Ranker(plugin, player, ratingName);
                return;
            }
        }
        if (e.getView().getTitle().contains(Prefix.PREFIX + "순위 (")) {
            e.setCancelled(true);
            if (e.getSlot() == 27) {
                ItemStack item = e.getCurrentItem();
                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

                List<String> lore = item.getItemMeta().getLore();

                String ratingName = ChatColor.stripColor(lore.get(0));
                ratingName = ratingName.replace(" - ","");
                Player player = (Player) e.getWhoClicked();
                GuiInventory.open_Record(plugin, player, ratingName);
            }
        }
    }
}
