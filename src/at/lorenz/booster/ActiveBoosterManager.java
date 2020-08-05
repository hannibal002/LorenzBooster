package at.lorenz.booster;

import at.lorenz.api.utils.TimeSetting;
import at.lorenz.api.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveBoosterManager implements Listener {

    private final BoosterPlugin plugin;
    private final Map<Booster, Long> boosters = new HashMap<>();
    private final Map<Booster, String> lastActivated = new HashMap<>();
    private long noFallDamageUntil = 0;

    public ActiveBoosterManager(BoosterPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20, 20);
    }

    private void tick() {
        for (Map.Entry<Booster, Long> entry : new ArrayList<>(boosters.entrySet())) {
            Booster booster = entry.getKey();
            long until = entry.getValue();
            long now = System.currentTimeMillis();
            if (now > until) {
                boosters.remove(booster);
                fadedOutBooster(booster);
            } else {

                int timeLeft = (int) ((until - now) / 1000);
                if (shouldBroadcast(timeLeft)) {
                    String format = Utils.formatTime(timeLeft, TimeSetting.SECONDS, TimeSetting.NO_BORDER, TimeSetting.HIDE_HOURS_IF_NOT_NEEDED);
                    String msg = BoosterPlugin.PREFIX + "§cDer Booster " + booster.getName() + " §cwird in §f" + format + " §causgeschaltet";
                    plugin.sendMessageToAll(msg);
                    for (Player player : plugin.getAllPlayers()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 100.0F, 1.0F);
                    }
                }
                if (timeLeft == BoosterPlugin.TIME_REACTIVATE) {
                    for (Player player : plugin.getAllPlayers()) {
                        if (plugin.getUserManager().hasBooster(player, booster)) {
                            TextComponent text = new TextComponent("klicke hier um den Booster sofort zu verlängern!");
                            String label = booster.getLabel().toLowerCase();
                            label = label.substring(2);
                            String command = "/activatebooster " + label;
                            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
                            text.setClickEvent(clickEvent);
                            player.spigot().sendMessage(text);
                        }
                    }
                }

                applyBooster(booster);
            }
        }
    }

    private boolean shouldBroadcast(int timeLeft) {
        switch (timeLeft) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 60:
            case 60 * 5:
                return true;
        }

        return false;
    }

    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isTestPlayer(player)) {
            for (Booster booster : boosters.keySet()) {
                booster.apply(player);
                player.sendMessage("§7Der " + booster.getName() + " §7ist aktiv!");
            }
        }
    }

    @EventHandler
    void onFallDmg(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {

            if (noFallDamageUntil > System.currentTimeMillis()) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                } else {
                    entity.sendMessage("dmg is " + event.getCause());
                }
            }
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (plugin.isTestPlayer(player)) {
            if (isActiveFor(player, Booster.XP)) {
                event.setAmount(event.getAmount() * 5);
            }
        }
    }

    private void fadedOutBooster(Booster booster) {
        plugin.sendMessageToAll(BoosterPlugin.PREFIX + "§cDer " + booster.getName() + " §cwurde ausgeschaltet!");
        for (Player player : plugin.getAllPlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 100.0F, 1.0F);
        }
        disableBooster(booster);
        if (booster == Booster.FLY) {
            noFallDamageUntil = System.currentTimeMillis() + 10_000;
        }
    }

    private void disableBooster(Booster booster) {
        if (booster == Booster.FLY) {
            noFallDamageUntil = System.currentTimeMillis() + 10_000;
        }
        for (Player player : plugin.getAllPlayers()) {
            booster.disable(player);
        }
        if (booster == Booster.FLY) {
            noFallDamageUntil = System.currentTimeMillis() + 10_000;
        }
    }

    private void applyBooster(Booster booster) {
        for (Player player : plugin.getAllPlayers()) {
            if (isActiveFor(player, booster)) {
                booster.apply(player);
            }
        }
    }

    public void activateBooster(Booster booster, Player player) {
        boolean reactivate = false;
        if (isActive(booster)) {
            reactivate = true;
            long time = boosters.get(booster);
            boosters.put(booster, time + BoosterPlugin.TIME_DURATION);
        } else {
            boosters.put(booster, System.currentTimeMillis() + BoosterPlugin.TIME_DURATION);
        }
        plugin.sync(() ->
                applyBooster(booster));

        lastActivated.put(booster, player.getName());

        String action = reactivate ? "verlängert" : "aktiviert";
        String message = BoosterPlugin.PREFIX + "§f" + player.getDisplayName() + "§a hat einen §6" + booster.getName() + " §a" + action + "!";
        for (Player all : plugin.getAllPlayers()) {
            if (plugin.getUserManager().isIgnored(all, booster)) {
                all.sendMessage(message + " §c(Ignoriert!)");
            } else {
                all.sendMessage(message);
            }
            all.playSound(all.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100.0F, 1.0F);
        }
    }

    public boolean isActive(Booster booster) {
        if (boosters.containsKey(booster)) {
            long until = boosters.get(booster);
            long now = System.currentTimeMillis();
            return until > now;
        }
        return false;
    }

    public boolean isActiveFor(Player player, Booster booster) {
        if (plugin.getUserManager().isIgnored(player, booster)) return false;
        return isActive(booster);
    }

    public void disableAll() {
        for (Booster booster : boosters.keySet()) {
            disableBooster(booster);
        }
    }

    public List<String> print() {
        List<String> list = new ArrayList<>();

        if (boosters.isEmpty()) {
            list.add("§7Es ist kein Booster aktiv!");
        } else {

            list.add("");
            if (boosters.size() == 1) {
                list.add("§7Es ist 1 Booster aktiv:");
            } else {
                list.add("§7Es sind " + boosters.size() + " Booster aktiv:");
            }

            for (Map.Entry<Booster, Long> entry : boosters.entrySet()) {
                Booster booster = entry.getKey();
                long until = entry.getValue();

                long delay = until - System.currentTimeMillis();
                String delayText = Utils.formatTime(delay / 1000, TimeSetting.NO_BORDER, TimeSetting.SECONDS, TimeSetting.HIDE_HOURS_IF_NOT_NEEDED);
                String from = lastActivated.get(booster);
                list.add(" §8- " + booster.getName() + " §7(" + delayText + ") von " + from);
            }
        }

        return list;
    }

    public long getRemainingBoosterTime(Booster booster) {
        if (!isActive(booster)) {
            return -1;
        }
        return boosters.get(booster) - System.currentTimeMillis();
    }
}
