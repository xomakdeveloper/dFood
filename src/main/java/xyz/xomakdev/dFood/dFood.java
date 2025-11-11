package xyz.xomakdev.dFood;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xomakdev.dFood.commands.MainCommand;
import xyz.xomakdev.dFood.listeners.PotionListener;

public final class dFood extends JavaPlugin {
    @Getter
    private static dFood instance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        getCommand("dfood").setExecutor(new MainCommand());
        getServer().getPluginManager().registerEvents(new PotionListener(), this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}