package at.lorenz.booster;

import at.lorenz.booster.transfer.SQLite;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TransferBoosterDataCommand implements CommandExecutor {

    private final BoosterPlugin plugin;

    public TransferBoosterDataCommand(BoosterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du musst ein Spieler sein!");
            return true;
        }

        Player player = (Player) sender;

        plugin.async(() -> {
            long start = System.currentTimeMillis();
            try {
                run();
                long duration = System.currentTimeMillis() - start;
                player.sendMessage("done after " + duration + " ms!");
            } catch (SQLException e) {
                e.printStackTrace();
                long duration = System.currentTimeMillis() - start;
                player.sendMessage("§cError after " + duration + " ms!");
            }
        });
        return true;
    }

    private void run() throws SQLException {
        plugin.getSqlManager().clearAll();

        SQLite sqLite = new SQLite();
        ResultSet resultSet = sqLite.getSQLConnection().createStatement().executeQuery("SELECT * FROM booster");
        LuckPerms api = LuckPermsProvider.get();
        UserManager userManager = api.getUserManager();


        Map<UUID, Map<Integer, Integer>> uuids = new HashMap<>();

        while (resultSet.next()) {
            String str = resultSet.getString("uuid");

            if (str.equals("81d9b78b-54ac-4506-b0f1-8b9218d4b966")) continue;//Schluchskopf

            UUID uuid = UUID.fromString(str);

            Map<Integer, Integer> booster = new HashMap<>();

            booster.put(0, resultSet.getInt("fly"));
            booster.put(1, resultSet.getInt("mining"));
            booster.put(2, resultSet.getInt("xp"));
            booster.put(3, resultSet.getInt("speed"));
            booster.put(4, resultSet.getInt("vision"));

            uuids.put(uuid, booster);
        }

        List<String> list = new ArrayList<>();

        for (UUID uuid : uuids.keySet()) {

            User user = userManager.getUser(uuid);
            if (user == null) {
                userManager.loadUser(uuid).thenAccept(u -> {
                    String primaryGroup = u.getPrimaryGroup();
                    if (!list.contains(primaryGroup)) {
                        list.add(primaryGroup);
                        System.out.println("list = " + list);
                    }
                });
                continue;
            }

            String primaryGroup = user.getPrimaryGroup();
            if (!list.contains(primaryGroup)) {
                list.add(primaryGroup);
            }
        }

        System.out.println("list = " + list);
        System.out.println("counter = " + uuids.size());

        int haveToDo = uuids.size();

        for (Map.Entry<UUID, Map<Integer, Integer>> entry : uuids.entrySet()) {
            haveToDo--;
            UUID uuid = entry.getKey();
            User user = userManager.getUser(uuid);
            if (user == null) {
                System.err.println("user is null: " + uuid);
                return;
            }

            String primaryGroup;
            try {
                primaryGroup = user.getPrimaryGroup();
            } catch (NullPointerException e) {
                System.err.println("user is null:" + uuid);
                continue;
            }
            switch (primaryGroup) {
                case "koenig":
                case "kaiser":
                case "prinz":
                case "test-hund":
                case "maus":
                    continue;
            }
            at.lorenz.booster.User u = new at.lorenz.booster.User(uuid);
            for (Map.Entry<Integer, Integer> e : entry.getValue().entrySet()) {
                int id = e.getKey();
                int amount = e.getValue();
                if (amount > 0) {
//                    System.out.println(uuid + " " + id + ": " + amount);
                    u.getMap().put(Booster.values()[id], amount);
                }
            }
            plugin.getSqlManager().update(u);
            System.out.println("updating " + haveToDo);
        }
    }
}
