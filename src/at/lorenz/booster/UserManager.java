package at.lorenz.booster;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserManager implements Listener {

    private final List<User> users = new ArrayList<>();
    private final SqlManager sqlManager;
    private final BoosterPlugin plugin;

    public UserManager(BoosterPlugin plugin, SqlManager sqlManager) {
        this.plugin = plugin;
        this.sqlManager = sqlManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        for (Player player : plugin.getAllPlayers()) {
            load(player);
        }
    }

    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isTestPlayer(player)) {
            load(player);
        }
    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.isTestPlayer(player)) {
            User user = getUser(player);
            users.remove(user);
        }
    }

    private void load(Player player) {
        plugin.async(() -> {
            User user = new User(player.getUniqueId());
            try {
                sqlManager.load(user);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                player.sendMessage("§cDeine Booster konnten nicht richtig geladen werden!");
            }
            users.add(user);
        });
    }

    private User getUser(Player player) {
        for (User user : users) {
            if (user.getUuid().equals(player.getUniqueId())) {
                return user;
            }
        }
        throw new RuntimeException("No user loaded for player " + player.getName());
    }

    public boolean hasBooster(Player player, Booster booster) {
        return getUser(player).hasBooster(booster);
    }

    public int getBoosters(Player player, Booster booster) {
        return getUser(player).getBoosters(booster);
    }

    public void useBooster(Player player, Booster booster) throws SQLException {
        User user = getUser(player);
        user.useBooster(booster);
        sqlManager.update(user);
    }

    public void addBooster(Player player, Booster booster) throws SQLException {
        User user = getUser(player);
        user.addBooster(booster);
        sqlManager.update(user);
    }

    public boolean isIgnored(Player player, Booster booster) {
        return getUser(player).isIgnored(booster);
    }

    public void toggleIgnored(Player player, Booster booster) throws SQLException {
        User user = getUser(player);
        if (user.toggleIgnored(booster)) {
            if (plugin.getActiveBoosterManager().isActive(booster)) {
                plugin.sync(() -> {
                    booster.disable(player);
                });
            }
        }
        sqlManager.update(user);
    }

    public void setBooster(Player target, Booster booster, int amount) throws SQLException {
        User user = getUser(target);
        user.getMap().put(booster, amount);
        sqlManager.update(user);

    }

    public List<String> getBoostersPrint(Player player) {
        List<String> list = new ArrayList<>();

        for (Booster booster : Booster.values()) {
            if (hasBooster(player, booster)) {
                int amount = getBoosters(player, booster);
                list.add(amount + "x " + booster.getName());
            }
        }
        if (list.isEmpty()) {
            list.add("§7" + player.getName() + " hat keine Booster.");
        } else {
            list.add(0, "§7" + player.getName() + " hat " + list.size() + " verschiedene Booster:");
        }

        return list;
    }
}
