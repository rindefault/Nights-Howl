package me.rin.nightshowl.items;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JSONLootable {

    private final String   name;
    private final Material material;
    private final int      customModelData;

    @Getter
    private final int      amountRange;

    @Getter
    private final int      chance;

    public JSONLootable(final String name, final Material material, final int customModelData, int amountRange, int chance) {

        this.name =                 name;
        this.material =             material;
        this.customModelData =      customModelData;
        this.amountRange =          amountRange;
        this.chance =               chance;

    }

    public ItemStack getItemStack() {

        ItemStack itemStack = new ItemStack(this.material, (int) (Math.random() * amountRange + 1));
        ItemMeta  itemMeta =      itemStack.getItemMeta();

        itemMeta.setDisplayName(this.name);
        itemMeta.setCustomModelData(this.customModelData);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
