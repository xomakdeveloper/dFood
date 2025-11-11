package xyz.xomakdev.dFood.potions;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.xomakdev.dFood.dFood;
import xyz.xomakdev.dFood.utils.ConfigurationUtil;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PotionEffectListener {

    private static final dFood plugin = dFood.getInstance();

    public static String getEffectDisplayName(PotionEffectType type) {
        String effectName = type.getKey().getKey().toUpperCase();
        return ConfigurationUtil.getString("effects.names." + effectName, effectName);
    }

    public static boolean isEffectAllowed(PotionEffect effect, Material foodType) {
        String effectName = effect.getType().getKey().getKey().toUpperCase();
        FileConfiguration config = plugin.getConfig();

        List<String> globalDisallowed = config.getStringList("effects.disallowed.global");
        if (globalDisallowed.contains(effectName)) {
            return false;
        }

        String foodPath = "effects.disallowed.food." + foodType.name();
        if (config.contains(foodPath)) {
            List<String> foodDisallowed = config.getStringList(foodPath + ".disallowed");
            return !foodDisallowed.contains(effectName);
        }

        return true;
    }

    public static List<PotionEffect> filterAllowedEffects(List<PotionEffect> effects, Material foodType) {
        List<PotionEffect> allowedEffects = new ArrayList<>();
        for (PotionEffect effect : effects) {
            if (isEffectAllowed(effect, foodType)) {
                allowedEffects.add(effect);
            }
        }
        return allowedEffects;
    }
}