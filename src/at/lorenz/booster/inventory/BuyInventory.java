package at.lorenz.booster.inventory;

import at.lorenz.api.utils.Utils;
import at.lorenz.booster.Booster;
import at.lorenz.booster.BoosterPlugin;
import at.lorenz.api.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class BuyInventory implements Listener {

    private final BoosterPlugin plugin;

    private final static String INVENTORY_NAME = "§4§lProjekt §f§l» §rBooster kaufen§c";
    private final static int SLOT_BACK = 8;

    public BuyInventory(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!inventory.getTitle().equals(INVENTORY_NAME)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getSlot();
        if (clickedSlot == SLOT_BACK) {
            plugin.playClickSound(player);
            plugin.openInventoryMain(player);
        }

        if (clickedSlot < Booster.values().length) {
            plugin.playClickSound(player);
            Booster booster = Booster.values()[clickedSlot];
            player.closeInventory();
            plugin.askBuyBooster(player, booster);
        }
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, INVENTORY_NAME);

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.CHORUS_FLOWER).name("§4Zurück").build());

        int slot = 0;
        for (Booster type : Booster.values()) {
            String name = type.getName();
            Material displayMaterial = type.getDisplayMaterial();
            ItemBuilder builder = new ItemBuilder(displayMaterial);
            builder.hideAllFlags();
            builder.name(name);
            builder.lore("", "§6Kosten: §f" + Utils.formatInteger(type.getPrice()) + " Kreuzer");

            inventory.setItem(slot, builder.build());
            slot++;
        }

        player.openInventory(inventory);

    }
}
