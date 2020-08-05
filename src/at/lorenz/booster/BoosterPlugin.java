package at.lorenz.booster;

import at.lorenz.api.MoneyAPI;
import at.lorenz.api.inventory.ConfirmInventory;
import at.lorenz.api.utils.TimeSetting;
import at.lorenz.api.utils.Utils;
import at.lorenz.booster.commands.*;
import at.lorenz.booster.inventory.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BoosterPlugin extends JavaPlugin {

    public static final String PREFIX = "§4§lProjekt §f§l» ";

    public static final long TIME_DURATION = 60_000 * 20;//TODO change time differently?
    public static final long TIME_TEAM_COOL_DOWN = 60_000 * 30;
    public static final long TIME_REACTIVATE = 60;

    private MainInventory mainInventory;
    private UseInventory useInventory;
    private BuyInventory buyInventory;
    private IgnoreInventory ignoreInventory;
    private DescriptionInventory descriptionInventory;

    @Getter
    private UserManager userManager;
    @Getter
    private ActiveBoosterManager activeBoosterManager;
    private SqlManager sqlManager;

    private long lastUnlimitedUseTime;
    private String lastUnlimitedUseText;

    @Getter
    @Setter
    private boolean running = true;

    @Override
    public void onEnable() {
        try {
            sqlManager = new SqlManager();
        } catch (SQLException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
            System.err.println("Could not start BoosterPlugin because of sql error");
            return;
        }

        userManager = new UserManager(this, sqlManager);
        activeBoosterManager = new ActiveBoosterManager(this);

        getCommand("booster2").setExecutor(new BoosterCommand(this));
        getCommand("togglebooster").setExecutor(new ToggleBoosterCommand(this));
        getCommand("activeboosters").setExecutor(new ActiveBoostersCommand(this));
        getCommand("setbooster").setExecutor(new GiveBoosterCommand(this));
        getCommand("getbooster").setExecutor(new GetBoosterCommand(this));
        getCommand("activatebooster").setExecutor(new ActivateBoosterCommand(this));
        getCommand("transferboosterdata").setExecutor(new TransferBoosterDataCommand(this));

        mainInventory = new MainInventory(this);
        useInventory = new UseInventory(this);
        buyInventory = new BuyInventory(this);
        ignoreInventory = new IgnoreInventory(this);
        descriptionInventory = new DescriptionInventory(this);
        Bukkit.getPluginManager().registerEvents(mainInventory, this);
        Bukkit.getPluginManager().registerEvents(useInventory, this);
        Bukkit.getPluginManager().registerEvents(buyInventory, this);
        Bukkit.getPluginManager().registerEvents(ignoreInventory, this);
        Bukkit.getPluginManager().registerEvents(descriptionInventory, this);
    }

    @Override
    public void onDisable() {
        activeBoosterManager.disableAll();
        try {
            sqlManager.disable();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("BoosterPlugin got an sql error during disable phase");
        }
    }

    public void openInventoryMain(Player player) {
        mainInventory.open(player);
    }

    public void openInventoryUse(Player player) {
        useInventory.open(player);
    }

    public void openInventoryBuy(Player player) {
        buyInventory.open(player);
    }

    public void openInventoryDisable(Player player) {
        ignoreInventory.open(player);
    }

    public void openInventoryDescription(Player player) {
        descriptionInventory.open(player);
    }

    public void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 100.0F, 1.0F);
    }

    public void askBuyBooster(Player player, Booster booster) {
        if (!MoneyAPI.hasMoney(player, booster.getPrice())) {
            player.sendMessage("§cDu hast nicht genug Geld!");
            playNoSound(player);
            return;
        }

        ConfirmInventory confirmInventory = new ConfirmInventory("Booster wirklich kaufen?");

        confirmInventory.setSuccess(() -> {
                    tryBuyBooster(player, booster);
                }, "Bestätigen", "§7Ich will einen ", "§f" + booster.getName() + " §7für",
                "§f" + booster.getPrice() + " Kreuzer §7kaufen.");

        confirmInventory.setFailure(() -> {
            playClickSound(player);
            openInventoryBuy(player);
        }, "Abbrechen", "§7Ich will §ckeinen §7Booster kaufen.");

        confirmInventory.open(this, player);
    }

    private void tryBuyBooster(Player player, Booster booster) {
        if (!MoneyAPI.hasMoney(player, booster.getPrice())) {
            player.sendMessage("§cDu hast nicht genug Geld!");
            playNoSound(player);
            return;
        }

        async(() -> {
            try {
                userManager.addBooster(player, booster);
            } catch (SQLException e) {
                e.printStackTrace();
                playNoSound(player);
                player.sendMessage("§cEs ist ein Datenbankfehler aufgetreten!");
                return;
            }

            MoneyAPI.withdrawMoney(player, booster.getPrice());

            player.sendMessage(PREFIX + "§aDu hast dir einen " + booster.getName() + " §agekauft!");
            playSoundPurchased(player);
            openInventoryBuy(player);
            log(player.getName() + " bought " + booster);
        });

    }

    public void askUseBooster(Player player, Booster booster) {
        if (!running) {
            player.sendMessage("§cBooster sind zurzeit deaktiviert!");
            return;
        }

        if (!hasUnlimited(player)) {
            if (!userManager.hasBooster(player, booster)) {
                player.sendMessage("§cDu hast keinen " + booster.getName() + " §cden du verwenden kannst!");
                return;
            }
        } else {
            long waitingTime = TIME_TEAM_COOL_DOWN;
            long duration = lastUnlimitedUseTime + waitingTime - System.currentTimeMillis();
            if (duration > 0) {
                long before = waitingTime - duration;
                player.sendMessage("§cDu kannst keinen Gratis-Booster aktivieren!");
                String beforeText = Utils.formatTime(before / 1000, TimeSetting.NO_BORDER, TimeSetting.SECONDS, TimeSetting.HIDE_HOURS_IF_NOT_NEEDED);

                player.sendMessage("§7Vor §f" + beforeText + " §7hat " + lastUnlimitedUseText);
                String durationText = Utils.formatTime(duration / 1000, TimeSetting.NO_BORDER, TimeSetting.SECONDS, TimeSetting.HIDE_HOURS_IF_NOT_NEEDED);
                player.sendMessage("§7In §f" + durationText + " §7kann der nächste Gratis-Booster aktiviert werden");
                return;
            }
        }

        boolean reactivate = false;

        if (activeBoosterManager.isActive(booster)) {
            long remainingTime = activeBoosterManager.getRemainingBoosterTime(booster);
            if (remainingTime < TIME_REACTIVATE * 1000) {
                reactivate = true;
            } else {
                player.sendMessage("§cEs ist bereits ein " + booster.getName() + " §caktiv!");
                return;
            }
        }

        String freeText = hasUnlimited(player) ? "gratis" : "";
        String action = reactivate ? "verlängern" : "aktivieren";

        ConfirmInventory confirmInventory = new ConfirmInventory("Booster wirklich " + action + "?");
        confirmInventory.setSuccess(() -> {
            useBooster(player, booster);
        }, "Bestätigen", "§7Ich will einen " + freeText, "§f" + booster.getName() + " §7" + action + ".");

        confirmInventory.setFailure(() -> {
            playClickSound(player);
            openInventoryUse(player);
        }, "Abbrechen", "§7Ich will §ckeinen §7Booster " + action + ".");

        confirmInventory.open(this, player);
    }

    private void useBooster(Player player, Booster booster) {
        if (!running) {
            player.sendMessage("§cBooster sind zurzeit deaktiviert!");
            playNoSound(player);
            return;
        }

        if (!hasUnlimited(player)) {
            if (!userManager.hasBooster(player, booster)) {
                player.sendMessage(PREFIX + "§cDu hast leider keine " + booster.getName() + " §cmehr. Bitte kaufe einen Neuen.");
                playNoSound(player);
                return;
            }
        }

        boolean reactivate = false;
        if (activeBoosterManager.isActive(booster)) {
            long remainingTime = activeBoosterManager.getRemainingBoosterTime(booster);
            if (remainingTime < TIME_REACTIVATE * 1000) {
                reactivate = true;
            } else {
                player.sendMessage("§cEs ist bereits ein " + booster.getName() + " §caktiv!");
                playNoSound(player);
                return;
            }
        }

        String action = reactivate ? "extended" : "activated";
        boolean finalReactivate = reactivate;
        async(() -> {
            try {
                if (!hasUnlimited(player)) {
                    userManager.useBooster(player, booster);
                    log(player.getName() + " " + action + " " + booster + " (not unlimited)");
                } else {
                    log(player.getName() + " " + action + " " + booster + " (unlimited)");
                    unlimitedUse(player, booster);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage("§cEs ist ein Datenbankfehler aufgetreten!");
                playNoSound(player);
                return;
            }

            activeBoosterManager.activateBooster(booster, player);
        });
    }

    private void unlimitedUse(Player player, Booster booster) {
        lastUnlimitedUseText = player.getName() + "  einen " + booster.getName() + " §7aktiviert.";
        lastUnlimitedUseTime = System.currentTimeMillis();
    }

    private void log(String text) {
        System.out.println("booster-log: " + text);
    }

    public List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isTestPlayer(player)) {
                list.add(player);
            }
        }

        return list;
    }

    //TODO remove
    public boolean isTestPlayer(Player player) {
        switch (player.getName()) {
            case "Forrick":
            case "hannibal2":
            case "Michael2011982":
            case "LukiX1":
                return true;
            default:
                return false;
        }
    }

    public void sendMessageToAll(String message) {
        for (Player player : getAllPlayers()) {
            player.sendMessage(message);
        }
    }

    public boolean hasUnlimited(Player player) {
//        return player.getGameMode() == GameMode.CREATIVE;
        return player.hasPermission("essentials.eco");
    }

    public void disableBoosters() {
        activeBoosterManager.disableAll();
    }

    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
    }

    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, runnable);
    }

    public void playNoSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
    }

    public void playSoundPurchased(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100.0F, 1.0F);
    }

    public SqlManager getSqlManager() {
        return sqlManager;
    }
}
