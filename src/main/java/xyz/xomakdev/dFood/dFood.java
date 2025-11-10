package xyz.xomakdev.dFood;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class dFood extends JavaPlugin {
    @Getter
    private static dFood instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
