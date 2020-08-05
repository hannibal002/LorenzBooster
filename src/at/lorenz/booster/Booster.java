package at.lorenz.booster;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Getter
public enum Booster {
    FLY("§9Fly", 10_000, Material.FEATHER, p -> {
        p.setAllowFlight(true);
    }, p -> {
        GameMode gameMode = p.getGameMode();
        if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR && !p.hasPermission("essentials.fly")) {
//        if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR) {
            p.setAllowFlight(false);
        } else {
            p.sendMessage("§7Du hast deinen Flugmodus behalten!");
        }
    }, new String[]{"Erlaubt das Fliegen.", "Überall."}),

    MINING("§7Abbau", 15_000, Material.DIAMOND_PICKAXE, p -> {
        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 20, 1, false, false));
    }, p -> {
        p.removePotionEffect(PotionEffectType.FAST_DIGGING);
    }, new String[]{"Lässt dich mit Eile 2", "durch Blöcke rasen."}),

    XP("§eXp", 7_500, Material.EXP_BOTTLE, p -> {
    }, p -> {
    }, new String[]{"Samlel 5x so viel", "Minecaft-Exp."}),

    SPEED("§aGeschwindigkeits", 5_000, Material.RABBIT_FOOT, p -> {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2, false, false));
    }, p -> {
        p.removePotionEffect(PotionEffectType.SPEED);
    }, new String[]{"Werde viel schneller dank", "dem Effekt Geschwindigkeit II."}),

    NIGHT_VISION("§1Nachtsicht", 5_000, Material.GOLDEN_CARROT, p -> {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 20, 0, false, false));
    }, p -> {
        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }, new String[]{"Sehe in der Nacht so", "gut wie ein Enderman."});

    private final String label;
    private final int price;
    private final Material displayMaterial;
    private final Consumer<Player> apply, disable;
    private final String[] descriontion;

    public static Booster fromName(String name) {
        for (Booster booster : values()) {
            String label = booster.label;
            String compare = label.substring(2);
            if (compare.equalsIgnoreCase(name)) {
                return booster;
            }
        }

        return null;
    }

    public String getName() {
        return label + " Booster";
    }

    public void apply(Player player) {
        apply.accept(player);
    }

    public void disable(Player player) {
        disable.accept(player);
    }
}
