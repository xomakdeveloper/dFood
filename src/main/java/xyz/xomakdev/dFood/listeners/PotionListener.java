package xyz.xomakdev.dFood.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.xomakdev.dFood.dFood;
import xyz.xomakdev.dFood.potions.PotionEffectListener;
import xyz.xomakdev.dFood.refactors.Refactor;
import xyz.xomakdev.dFood.utils.ConfigurationUtil;
import xyz.xomakdev.dFood.utils.NBTUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PotionListener implements Listener {

    private final dFood plugin = dFood.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        if (isSplashPotion(item)) {
            event.setCancelled(true);
            applyPotionEffectsToHeldItem(event.getPlayer(), item);
        } else if (isFood(item)) {
            event.setCancelled(true);
            consumeFoodWithEffects(event.getPlayer(), item);
        }
    }

    private boolean isSplashPotion(ItemStack item) {
        return item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION;
    }

    private boolean isFood(ItemStack item) {
        return item.getType().isEdible() && NBTUtil.hasPotionEffects(item);
    }

    private void applyPotionEffectsToHeldItem(Player player, ItemStack potion) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ItemStack targetItem = null;

        if (mainHand.getType().isEdible()) {
            targetItem = mainHand;
        } else if (offHand.getType().isEdible()) {
            targetItem = offHand;
        }

        if (targetItem == null) {
            return;
        }

        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        if (potionMeta == null) return;

        List<PotionEffect> effects = new ArrayList<>(potionMeta.getCustomEffects());

        if (effects.isEmpty()) {
            PotionEffectType baseEffect = potionMeta.getBasePotionType().getEffectType();
            if (baseEffect != null) {
                effects.add(new PotionEffect(baseEffect, 3600, 0));
            }
        }

        if (!effects.isEmpty()) {
            List<PotionEffect> allowedEffects = PotionEffectListener.filterAllowedEffects(effects, targetItem.getType());

            for (PotionEffect effect : effects) {
                if (!allowedEffects.contains(effect)) {
                    String effectName = PotionEffectListener.getEffectDisplayName(effect.getType());
                    player.sendMessage(ConfigurationUtil.mm(ConfigurationUtil.getString("messages.errors.effect_disallowed")
                            .replace("%effect%", effectName)));
                }
            }

            if (!allowedEffects.isEmpty()) {
                addEffectsToFood(targetItem, allowedEffects);
                player.getInventory().removeItem(potion);
                player.swingMainHand();
            }
        }
    }

    private void addEffectsToFood(ItemStack food, List<PotionEffect> effects) {
        List<PotionEffect> existingEffects = NBTUtil.getPotionEffects(food);
        existingEffects.addAll(effects);

        NBTUtil.setPotionEffects(food, existingEffects);
        updateFoodLore(food, existingEffects);
    }

    private void updateFoodLore(ItemStack food, List<PotionEffect> effects) {
        ItemMeta meta = food.getItemMeta();
        if (meta == null) return;

        Refactor refactor = Refactor.loadFromConfig();
        List<Component> lore = new ArrayList<>();

        for (PotionEffect effect : effects) {
            String effectName = PotionEffectListener.getEffectDisplayName(effect.getType());
            String formattedEffect = refactor.formatEffect(effectName, effect.getAmplifier(), effect.getDuration());
            lore.add(ConfigurationUtil.mm(formattedEffect));
        }

        meta.lore(lore);
        food.setItemMeta(meta);
    }

    private void consumeFoodWithEffects(Player player, ItemStack food) {
        if (player.getFoodLevel() >= 20 && !player.hasPermission("dfood.override.full")) {
            return;
        }

        List<PotionEffect> effects = NBTUtil.getPotionEffects(food);
        if (effects.isEmpty()) {
            return;
        }

        applyEffectsToPlayer(player, effects, food);
        consumeFoodItem(player, food);
    }

    private void applyEffectsToPlayer(Player player, List<PotionEffect> effects, ItemStack food) {
        boolean disableNotification = ConfigurationUtil.getBoolean("messages.added.disable_notification");
        boolean disableStart = ConfigurationUtil.getBoolean("messages.added.disable_start");

        if (!disableNotification) {
            Refactor refactor = Refactor.loadFromConfig();
            List<Component> messages = new ArrayList<>();

            if (!disableStart) {
                String foodName = getFoodDisplayName(food);
                String startText = ConfigurationUtil.getString("messages.added.start_text")
                        .replace("%food%", foodName);
                messages.add(ConfigurationUtil.mm(startText));
            }

            for (PotionEffect effect : effects) {
                String effectName = PotionEffectListener.getEffectDisplayName(effect.getType());
                String formattedEffect = refactor.formatEffect(effectName, effect.getAmplifier(), effect.getDuration());
                String addedText = ConfigurationUtil.getString("messages.added.added_text")
                        .replace("%effect_ref%", formattedEffect);
                messages.add(ConfigurationUtil.mm(addedText));
            }

            for (Component message : messages) {
                player.sendMessage(message);
            }
        }

        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }
    }

    private void consumeFoodItem(Player player, ItemStack food) {
        if (food.getAmount() > 1) {
            food.setAmount(food.getAmount() - 1);
        } else {
            player.getInventory().removeItem(food);
        }

        player.setFoodLevel(Math.min(20, player.getFoodLevel() + 4));
        player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + 2.4F));
    }

    private String getFoodDisplayName(ItemStack food) {
        if (food.hasItemMeta() && food.getItemMeta().hasDisplayName()) {
            return ConfigurationUtil.mmS(food.getItemMeta().displayName());
        }
        return food.getType().toString().toLowerCase().replace("_", " ");
    }
}