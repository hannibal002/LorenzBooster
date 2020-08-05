package at.lorenz.booster.commands;

import at.lorenz.booster.BoosterPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BoosterCommand implements TabExecutor {

    private final BoosterPlugin plugin;

    public BoosterCommand(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du musst ein Spieler sein!");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.isTestPlayer(player)) {
            player.sendMessage("§cAn Diesem Befehl wird noch gearbeit!");
            return true;
        }

        plugin.openInventoryMain(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        System.err.println("onTabComplete");
        return list;
    }
}
