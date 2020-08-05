package at.lorenz.booster.commands;

import at.lorenz.booster.BoosterPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetBoosterCommand implements CommandExecutor {

    private final BoosterPlugin plugin;

    public GetBoosterCommand(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du musst ein Spieler sein!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("booster.give")) {
            player.sendMessage("§cDu hast keine Rechte!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cVerwendung: /" + label + " <Spieler>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !player.canSee(target)) {
            player.sendMessage("§cDer Spieler ist nicht online!");
            return true;
        }

        if (plugin.hasUnlimited(target)) {
            player.sendMessage("§7" + target.getName() + " hat unendlich Booster!");
            return true;
        }

        for (String line : plugin.getUserManager().getBoostersPrint(target)) {
            player.sendMessage(line);
        }

        return true;
    }
}
