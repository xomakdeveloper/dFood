package xyz.xomakdev.dFood.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion)) {
            return;
        }

        ThrownPotion potion = (ThrownPotion) event.getEntity();
        Item nearestFoodItem = findNearestFoodItem(potion);

        if (nearestFoodItem == null) {
            return;
        }

        ItemStack foodStack = nearestFoodItem.getItemStack();
        if (!foodStack.getType().isEdible()) {
            return;
        }

        List<PotionEffect> effects = new ArrayList<>(potion.getEffects());

        if (effects.isEmpty() && potion.getPotionMeta() != null) {
            PotionEffectType baseEffect = potion.getPotionMeta().getBasePotionType().getEffectType();
            if (baseEffect != null) {
                effects.add(new PotionEffect(baseEffect, 3600, 0));
            }
        }

        if (effects.isEmpty()) {
            return;
        }

        List<PotionEffect> allowedEffects = PotionEffectListener.filterAllowedEffects(effects, foodStack.getType());

        if (!allowedEffects.isEmpty()) {
            addEffectsToFood(foodStack, allowedEffects);
            nearestFoodItem.setItemStack(foodStack);
            potion.remove();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().getType().isEdible()) {
            return;
        }

        ItemStack food = event.getItem();
        if (!NBTUtil.hasPotionEffects(food)) {
            return;
        }

        event.setCancelled(true);
        consumeFoodWithEffects(event.getPlayer(), food);
    }

    private Item findNearestFoodItem(ThrownPotion potion) {
        double maxRadius = ConfigurationUtil.getDouble("settings.max_potion_radius", 3.0);
        Item nearestItem = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Item item : potion.getWorld().getEntitiesByClass(Item.class)) {
            if (!item.getItemStack().getType().isEdible()) {
                continue;
            }

            double distance = item.getLocation().distance(potion.getLocation());
            if (distance <= maxRadius && distance < nearestDistance) {
                nearestDistance = distance;
                nearestItem = item;
            }
        }

        return nearestItem;
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

    private void consumeFoodWithEffects(org.bukkit.entity.Player player, ItemStack food) {
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

    private void applyEffectsToPlayer(org.bukkit.entity.Player player, List<PotionEffect> effects, ItemStack food) {
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

    private void consumeFoodItem(org.bukkit.entity.Player player, ItemStack food) {
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