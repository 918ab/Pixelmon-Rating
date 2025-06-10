package main.randomchest.Static;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
public class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name) {
        itemMeta.setDisplayName(name);
        return this;
    }
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }
    public ItemBuilder addLore(String loreLine) {
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(loreLine);
        itemMeta.setLore(lore);
        return this;
    }
    public ItemBuilder setCustomModelData(Integer modeldata){
        itemMeta.setCustomModelData(modeldata);
        return this;
    }

    public ItemBuilder setLore(List<String> loreLines) {
        itemMeta.setLore(loreLines);
        return this;
    }


    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }


//    ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
//            .setDisplayName("강력한 검")
//            .addLore("이 검은 매우 강력합니다.")
//            .addLore("사용할 때 주의하세요.")
//            .build();
}
