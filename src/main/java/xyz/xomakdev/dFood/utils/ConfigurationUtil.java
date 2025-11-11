package xyz.xomakdev.dFood.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.xomakdev.dFood.dFood;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationUtil {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    public static MiniMessage mm() { return mm; }
    public static Component mm(String message) { return mm.deserialize(pref(message)); }
    public static String mmS(Component message) { return mm.serialize(message); }
    public static String pref(String message) { return message.replace("%prefix%", getString("prefix", "")); }
    public static FileConfiguration getConfig() { return dFood.getInstance().getConfig(); }
    public static String getString(String path, String def) {
        String result = getConfig().getString(path);
        return result != null ? result : def;
    }
    public static String getString(String path) { return getString(path, ""); }
    public static Integer getInt(String path, Integer def) { return getConfig().getInt(path, def != null ? def : 0); }
    public static Integer getInt(String path) { return getInt(path, 0); }
    public static Double getDouble(String path, Double def) { return getConfig().getDouble(path, def != null ? def : 0.0); }
    public static Double getDouble(String path) { return getDouble(path, 0.0); }
    public static Boolean getBoolean(String path, Boolean def) { return getConfig().getBoolean(path, def != null ? def : false); }
    public static Boolean getBoolean(String path) { return getBoolean(path, false); }
    public static List getList(String path, List def) {
        List<?> result = getConfig().getList(path);
        return result != null ? result : (def != null ? def : Collections.emptyList());
    }
    public static List<?> getList(String path) { return getList(path, Collections.emptyList()); }
    public static List<String> getStringList(String path, List<String> def) {
        List<String> result = getConfig().getStringList(path);
        return !result.isEmpty() ? result : (def != null ? def : Collections.emptyList());
    }
    public static List<String> getStringList(String path) { return getStringList(path, Collections.emptyList()); }
    public static List<Component> getComponentList(String path, List<Component> def) {
        List<String> stringList = getConfig().getStringList(path);
        if (!stringList.isEmpty()) return stringList.stream().map(mm::deserialize).collect(Collectors.toList());
        return def != null ? def : Collections.emptyList();
    }
    public static List<Component> getComponentList(String path) { return getComponentList(path, Collections.emptyList()); }

    //tolko dlua plugina, ne standart:
    public static List<String> getDisallowedEffects(String foodType) {
        if (foodType == null) {
            return getStringList("effects.disallowed.global");
        }
        return getStringList("effects.disallowed.food." + foodType + ".disallowed");
    }
}