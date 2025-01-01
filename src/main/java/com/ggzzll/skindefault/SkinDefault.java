package com.ggzzll.skindefault;

import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.property.InputDataResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public final class SkinDefault extends JavaPlugin implements Listener {

    YamlConfiguration Config;
    private SkinsRestorer SkinsRestorerAPI;

    @Override
    public void onEnable() {
        getLogger().info(getDescription().getName() + " 已启用！");

        if (!VersionProvider.isCompatibleWith("15")) {
            getLogger().info("温馨提示: 插件最好使用15以上的版本，不然可能会存在些许问题，你现在安装的版本是" + VersionProvider.getVersionInfo());
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();

        File File = new File(getDataFolder(), "config.yml");
        Config = YamlConfiguration.loadConfiguration(File);

        SkinsRestorerAPI = SkinsRestorerProvider.get();
        getLogger().info(getDescription().getName() + " 初始化完成！");
        getServer().getPluginManager().registerEvents(this, this);
    }



    @Override
    public boolean onCommand(CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {
        if (Sender.hasPermission("SkinDefault.Reload")) {

            File File = new File(getDataFolder(), "config.yml");
            Config = YamlConfiguration.loadConfiguration(File);

            Sender.sendMessage("插件配置重载完成");
            return true;

        }

        Sender.sendMessage("你没有权限执行这个指令");
        return true;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent Event) {
        Player Player = Event.getPlayer();

        if (SkinsRestorerAPI.getPlayerStorage().getSkinOfPlayer(Player.getUniqueId()).isEmpty()){

            String SkinDefault = Config.getStringList("SkinDefault").get((int)(Math.random() * Config.getStringList("SkinDefault").size()));
            Optional<InputDataResult> SteveSkin = SkinsRestorerAPI.getSkinStorage().findSkinData(SkinDefault);

            if (SteveSkin.isEmpty()) {
                getLogger().info(" 玩家 " + Player.getName() + " 在进入服务器时设置 " + SkinDefault + "皮肤失败，原因：皮肤ID不存在");
                return;
            }

            SkinsRestorerAPI.getPlayerStorage().setSkinIdOfPlayer(Player.getUniqueId(), SteveSkin.get().getIdentifier());
            getLogger().info(" 玩家 " + Player.getName() + " 在进入服务器时设置 " + SkinDefault + "皮肤成功");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " 感谢您的使用，如果可以请给该项目点一个Star吧！！！期待您的下次使用");
    }
}
