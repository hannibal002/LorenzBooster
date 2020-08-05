package at.lorenz.booster.commands;

import at.lorenz.booster.Booster;
import at.lorenz.booster.BoosterPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ActivateBoosterCommand implements TabExecutor {

    private final BoosterPlugin plugin;

    public ActivateBoosterCommand(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du musst ein Spieler sein!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("§cVerwendung: /" + label + " <Booster>");
            return true;
        }

        Booster booster = Booster.fromName(args[0]);
        if (booster == null) {
            player.sendMessage("§cDiesen Booster gibt es nicht!");
            return true;
        }

        if (!plugin.hasUnlimited(player)) {
            if (!plugin.getUserManager().hasBooster(player, booster)) {
                player.sendMessage("§cDu hast keinen " + booster.getName() + " §cden du verwenden kannst!");
                plugin.playNoSound(player);
                return true;
            }
        }

        plugin.askUseBooster(player, booster);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            for (Booster booster : Booster.values()) {
                String name = booster.getLabel().substring(2);
                list.add(name);
            }
        }

        return list;
    }
}
