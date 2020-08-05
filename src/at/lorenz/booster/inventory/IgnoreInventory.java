package at.lorenz.booster.inventory;

import at.lorenz.api.inventory.ItemBuilder;
import at.lorenz.booster.Booster;
import at.lorenz.booster.BoosterPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;

public class IgnoreInventory implements Listener {

    private final BoosterPlugin plugin;

    private final static String INVENTORY_NAME = "§4§lProjekt §f§l» §rBooster ignorieren§c";
    private final static int SLOT_BACK = 8;

    public IgnoreInventory(BoosterPlugin plugin) {
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


            plugin.async(() -> {
                try {
                    plugin.getUserManager().toggleIgnored(player, booster);
                    if (plugin.getUserManager().isIgnored(player, booster)) {
                        player.sendMessage("§7Du ignorierst nun den " + booster.getName() + "§7.");
                    } else {
                        player.sendMessage("§7Du ignorierst nun den " + booster.getName() + " §7nicht mehr.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    player.sendMessage("§cEs ist ein Datenbankfehler aufgetreten beim Speichern von den ignorierten Boostern!");
                }
            });
        }
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, INVENTORY_NAME);

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.CHORUS_FLOWER).name("§4Zurück").build());

        int slot = 0;
        for (Booster booster : Booster.values()) {
            String name = booster.getName();
            Material displayMaterial = booster.getDisplayMaterial();
            ItemBuilder builder = new ItemBuilder(displayMaterial);
            builder.hideAllFlags();
            builder.name(name);
            if (plugin.getUserManager().isIgnored(player, booster)) {
                builder.lore("", "§cWird ignoriert.");
            } else {
                builder.lore("", "§aWird nicht ignoriert.");
            }

            inventory.setItem(slot, builder.build());
            slot++;
        }

        player.openInventory(inventory);

    }
}
