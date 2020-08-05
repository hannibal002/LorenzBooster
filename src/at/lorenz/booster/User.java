package at.lorenz.booster;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class User {

    private final UUID uuid;
    @Getter
    private final Map<Booster, Integer> map = new HashMap<>();
    private final List<Booster> ignoredBoosters = new ArrayList<>();

    public boolean hasBooster(Booster booster) {
        return getBoosters(booster) > 0;
    }

    public void useBooster(Booster booster) {
        if (!hasBooster(booster)) throw new RuntimeException("there is no booster!");
        int amount = map.getOrDefault(booster, 0);
        map.put(booster, amount - 1);
    }

    public int getBoosters(Booster booster) {
        return map.getOrDefault(booster, 0);
    }

    public void addBooster(Booster booster) {
        map.put(booster, getBoosters(booster) + 1);
    }

    public boolean isIgnored(Booster booster) {
        return ignoredBoosters.contains(booster);
    }

    public boolean toggleIgnored(Booster booster) {
        if (ignoredBoosters.contains(booster)) {
            ignoredBoosters.remove(booster);
            return false;
        } else {
            ignoredBoosters.add(booster);
            return true;
        }
    }
}
