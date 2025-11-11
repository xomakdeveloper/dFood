package xyz.xomakdev.dFood.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.xomakdev.dFood.dFood;

import java.util.ArrayList;
import java.util.List;

public class NBTUtil {
    private static final NamespacedKey EFFECTS_KEY = new NamespacedKey(dFood.getInstance(), "effects");

    public static void setPotionEffects(ItemStack item, List<PotionEffect> effects) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String effectsData = effects.stream()
                .map(effect -> effect.getType().getKey().getKey() + ":" + effect.getDuration() + ":" + effect.getAmplifier())
                .reduce((a, b) -> a + ";" + b)
                .orElse("");

        pdc.set(EFFECTS_KEY, PersistentDataType.STRING, effectsData);
        item.setItemMeta(meta);
    }

    public static List<PotionEffect> getPotionEffects(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return new ArrayList<>();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String effectsData = pdc.get(EFFECTS_KEY, PersistentDataType.STRING);
        if (effectsData == null || effectsData.isEmpty()) return new ArrayList<>();

        List<PotionEffect> effects = new ArrayList<>();
        String[] effectEntries = effectsData.split(";");

        for (String entry : effectEntries) {
            String[] parts = entry.split(":");
            if (parts.length == 3) {
                try {
                    PotionEffectType type = PotionEffectType.getByName(parts[0]);
                    int duration = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);

                    if (type != null) {
                        effects.add(new PotionEffect(type, duration, amplifier));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return effects;
    }

    public static boolean hasPotionEffects(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(EFFECTS_KEY, PersistentDataType.STRING);
    }

    public static void clearPotionEffects(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(EFFECTS_KEY);
        item.setItemMeta(meta);
    }
}