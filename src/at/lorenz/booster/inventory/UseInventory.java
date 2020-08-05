package at.lorenz.booster.inventory;

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

public class UseInventory implements Listener {

    private final BoosterPlugin plugin;

    private final static String INVENTORY_NAME = "§4§lProjekt §f§l» §rBooster benutzen§c";
    private final static int SLOT_BACK = 8;

    public UseInventory(BoosterPlugin plugin) {
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

            if (!plugin.hasUnlimited(player)) {
                if (!plugin.getUserManager().hasBooster(player, booster)) {
                    player.sendMessage("§cDu hast keinen " + booster.getName() + " §cden du verwenden kannst!");
                    plugin.playNoSound(player);
                    return;
                }
            }

            player.closeInventory();
            plugin.askUseBooster(player, booster);
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
            if (plugin.hasUnlimited(player)) {
                builder.lore("", "§aDu kannst alle Booster", "§agratis aktivieren.");
            } else {
                int amount = plugin.getUserManager().getBoosters(player, booster);
                builder.lore("", "§6Im Besitz: §f" + amount);
            }

            inventory.setItem(slot, builder.build());
            slot++;
        }

        player.openInventory(inventory);
    }
}
