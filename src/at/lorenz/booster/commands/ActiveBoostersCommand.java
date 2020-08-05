package at.lorenz.booster.commands;

import at.lorenz.booster.ActiveBoosterManager;
import at.lorenz.booster.BoosterPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActiveBoostersCommand implements CommandExecutor {

    private final BoosterPlugin plugin;

    public ActiveBoostersCommand(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du musst ein Spieler sein!");
            return true;
        }

        Player player = (Player) sender;

        ActiveBoosterManager activeBoosterManager = plugin.getActiveBoosterManager();
        for (String line : activeBoosterManager.print()) {
            player.sendMessage(line);
        }

        return true;
    }
}
