package at.lorenz.booster.commands;

import at.lorenz.api.inventory.ConfirmInventory;
import at.lorenz.booster.BoosterPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleBoosterCommand implements CommandExecutor {

    private final BoosterPlugin plugin;

    public ToggleBoosterCommand(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du musst ein Spieler sein!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("booster.toggle")) {
            player.sendMessage("§cDu hast dazu keine Rechte!");
            return true;
        }

        if (plugin.isRunning()) {
            tryDisable(player);
        } else {
            tryEnable(player);
        }

        return true;
    }

    private void tryDisable(Player player) {
        ConfirmInventory confirmInventory = new ConfirmInventory("Booster wirklich deaktivieren?");

        confirmInventory.setSuccess(() -> {
                    if (!plugin.isRunning()) {
                        player.sendMessage("§cDie Booster sind bereits deaktiviert!");
                        return;
                    }

                    plugin.disableBoosters();
                    plugin.setRunning(false);
                    player.sendMessage("§7Du hast alle Booster deaktiviert. Um die Booster wieder zu aktivieren, mache erneut /togglebooster.");

                    plugin.sendMessageToAll("§lAlle Booster wurden §c§ldeaktiviert!");
                }, "§c§lBOOSTER DEAKTIVIEREN", "§7Willst du wirklich alle",
                "§7aktiven Booster deaktivieren",
                "§7und zukünftige booster blockieren?");

        confirmInventory.setFailure(() -> {
        }, "§7§lAbbrechen");

        confirmInventory.open(plugin, player);
    }

    private void tryEnable(Player player) {
        ConfirmInventory confirmInventory = new ConfirmInventory("Booster wieder aktivieren?");

        confirmInventory.setSuccess(() -> {
                    if (plugin.isRunning()) {
                        player.sendMessage("§cDie Booster sind bereits aktiviert worden!");
                        return;
                    }

                    plugin.setRunning(true);
                    plugin.sendMessageToAll("§lBooster wurden wieder §a§laktiviert!");

                    plugin.disableBoosters();
                    player.sendMessage("§7Du hast die Booster wieder aktiviert.");
                }, "§a§lBOOSTER AKTIVIEREN", "§7Aktiviert das Booster-Feature wieder.");

        confirmInventory.setFailure(() -> {
        }, "§c§lDeaktiviert lassen");

        confirmInventory.open(plugin, player);
    }
}
