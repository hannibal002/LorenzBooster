package at.lorenz.booster.commands;

import at.lorenz.booster.Booster;
import at.lorenz.booster.BoosterPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GiveBoosterCommand implements TabExecutor {

    private final BoosterPlugin plugin;

    public GiveBoosterCommand(BoosterPlugin plugin) {
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

        if (args.length != 3) {
            player.sendMessage("§cVerwendung: /" + label + " <Spieler> <Booster> <Anzahl>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !player.canSee(target)) {
            player.sendMessage("§cDer Spieler ist nicht online!");
            return true;
        }

        Booster booster = Booster.fromName(args[1]);
        if (booster == null) {
            player.sendMessage("§cDiesen Booster gibt es nicht!");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cUngültige Zahl!");
            return true;
        }

        if (amount < 1 || amount > 100) {
            player.sendMessage("§cDie Anzahl muss zwischen 1 und 100 liegen");
            return true;
        }


        plugin.async(() -> {
            try {
                plugin.getUserManager().setBooster(target, booster, amount);
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage("§cEs ist ein Datenbankfehler aufgetreten! Die Anzahl an Boostern konnte nicht richtig gespeichert werden!");
                return;
            }

            player.sendMessage("§7Du hast " + target.getName() + " die Anzahl von " + booster.getName() + " §7auf " + amount + " gesetzt");
            if (!player.getName().equals(target.getName())) {
                target.sendMessage("§7Dir wurde die Anzahl an " + booster.getName() + " §7von " + player.getName() + " auf " + amount + " gesetzt.");
            }

        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : plugin.getAllPlayers()) {
                list.add(player.getName());
            }
        }

        if (args.length == 2) {
            for (Booster booster : Booster.values()) {
                String name = booster.getLabel().substring(2);
                list.add(name);
            }
        }

        return list;
    }
}
