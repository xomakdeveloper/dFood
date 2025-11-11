package xyz.xomakdev.dFood.commands;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import xyz.xomakdev.dFood.dFood;
import xyz.xomakdev.dFood.utils.ConfigurationUtil;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MainCommand implements CommandExecutor, TabExecutor {

    private final dFood plugin = dFood.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("dfood.reload")) {
                    sender.sendMessage(ConfigurationUtil.mm("<red>У вас нет прав на эту команду!"));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ConfigurationUtil.mm(ConfigurationUtil.getString("messages.reload")));
                break;

            case "help":
                sendHelp(sender);
                break;

            case "version":
                if (!sender.hasPermission("dfood.version")) {
                    sender.sendMessage(ConfigurationUtil.mm("<red>У вас нет прав на эту команду!"));
                    return true;
                }
                sendVersion(sender);
                break;

            default:
                sender.sendMessage(ConfigurationUtil.mm("<red>Неизвестная команда. Используйте /dfood help"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "help", "version").stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        if (!sender.hasPermission("dfood.help")) {
            sender.sendMessage(ConfigurationUtil.mm("<red>У вас нет прав на эту команду!"));
            return;
        }

        List<Component> helpMessages = ConfigurationUtil.getComponentList("messages.help");
        for (Component message : helpMessages) {
            sender.sendMessage(message);
        }
    }

    private void sendVersion(CommandSender sender) {
        List<Component> versionMessages = ConfigurationUtil.getComponentList("messages.version");
        for (Component message : versionMessages) {
            String processedMessage = ConfigurationUtil.mmS(message)
                    .replace("{projectName}", plugin.getName())
                    .replace("{projectVersion}", plugin.getDescription().getVersion())
                    .replace("{projectAuthors}", String.join(", ", plugin.getDescription().getAuthors()));
            sender.sendMessage(ConfigurationUtil.mm(processedMessage));
        }
    }
}