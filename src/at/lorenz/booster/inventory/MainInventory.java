package at.lorenz.booster.inventory;

import at.lorenz.booster.BoosterPlugin;
import at.lorenz.api.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MainInventory implements Listener {

    private final BoosterPlugin plugin;

    private final static String INVENTORY_NAME = "§4§lProjekt §f§l» §rBooster§c";
    private final static int SLOT_CLOSE = 8;
    private final static int SLOT_USE = 0;
    private final static int SLOT_BUY = 2;
    private final static int SLOT_DESCRIPTION = 4;
    private final static int SLOT_DISABLE = 6;

    public MainInventory(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!inventory.getTitle().equals(INVENTORY_NAME)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        if (event.getSlot() == SLOT_CLOSE) {
            player.closeInventory();
        }

        if (event.getSlot() == SLOT_USE) {
            plugin.playClickSound(player);
            plugin.openInventoryUse(player);
        }

        if (event.getSlot() == SLOT_BUY) {
            if (!plugin.hasUnlimited(player)) {
                plugin.playClickSound(player);
                plugin.openInventoryBuy(player);
            } else {
                plugin.playNoSound(player);
            }
        }

        if (event.getSlot() == SLOT_DISABLE) {
            plugin.playClickSound(player);
            plugin.openInventoryDisable(player);
        }

        if (event.getSlot() == SLOT_DESCRIPTION) {
            plugin.playClickSound(player);
            plugin.openInventoryDescription(player);
        }
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, INVENTORY_NAME);

        inventory.setItem(SLOT_USE, new ItemBuilder(Material.FIREWORK).name("§9Booster benutzen").build());

        if (plugin.hasUnlimited(player)) {
            inventory.setItem(SLOT_BUY, new ItemBuilder(Material.GOLD_INGOT).name("§6Booster kaufen")
                    .lore("", "§aDu darfst alle Booster",
                            "§aumsont verwenden und",
                            "§akannst daher keine",
                            "§aBooster kaufen!").build());
        } else {
            inventory.setItem(SLOT_BUY, new ItemBuilder(Material.GOLD_INGOT).name("§6Booster kaufen").build());
        }

        inventory.setItem(SLOT_DESCRIPTION, new ItemBuilder(Material.BOOK).name("§3Beschreibung")
                .lore("", "§7Für eine Erklärung",
                        "§7was die Booster machen",
                        "§7hier klicken.").build());

        inventory.setItem(SLOT_DISABLE, new ItemBuilder(Material.BIRCH_DOOR_ITEM).name("§eBooster ingorieren")
                .lore("",
                        "§7Du kannst einzelne",
                        "§7Booster deaktivieren sodass",
                        "§7der Effekt für dich nicht wirkt.").build());

        inventory.setItem(SLOT_CLOSE, new ItemBuilder(Material.CHORUS_PLANT).name("§4Schließen").build());

        player.openInventory(inventory);

    }
}
