package com.ggzzll.skindefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.MojangSkinDataResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public final class SkinDefault extends JavaPlugin implements Listener {

    FileConfiguration Config = getConfig();
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

        SkinsRestorerAPI = SkinsRestorerProvider.get();
        getLogger().info(getDescription().getName() + " 初始化完成！");

        if (Config.getBoolean("Auto-Updates")){
            String apiUrl = "https://gitee.com/api/v5/repos/Mortal1063/SkinDefault/tags";
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    JsonObject Data = JsonParser.parseString(in.lines().collect(Collectors.joining())).getAsJsonArray().get(0).getAsJsonObject();

                    if (!Data.get("name").getAsString().equals(getDescription().getVersion())) {
                        getLogger().info("版本更新，更新地址: https://gitee.com/Mortal1063/SkinDefault/releases/, 更新内容: " + Data.get("message").getAsString());
                    }

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        getServer().getPluginManager().registerEvents(this, this);
    }



    @Override
    public boolean onCommand(CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {
        if (Sender.hasPermission("SkinDefault.Reload")) {

            reloadConfig();
            Config = getConfig();

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

            String SkinName = Config.getStringList("SkinDefault").get(new Random().nextInt(Config.getStringList("SkinDefault").size()));
            Optional<InputDataResult> PlayerSkin = SkinsRestorerAPI.getSkinStorage().findSkinData(SkinName);

            if (PlayerSkin.isEmpty()) {

                try {
                    Optional<MojangSkinDataResult> MojangPlayerSkin = SkinsRestorerAPI.getSkinStorage().getPlayerSkin(SkinName, true);

                    if (MojangPlayerSkin.isEmpty()) {

                        getLogger().info(" 玩家 " + Player.getName() + " 在进入服务器时设置 " + SkinName + "皮肤失败，原因：皮肤ID不存在");
                        return;

                    } else {

                        MojangSkinDataResult MojangPlayer = MojangPlayerSkin.get();

                        String apiUrl = "https://api.mojang.com/user/profile/" + MojangPlayer.getUniqueId();
                        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);

                        String MojangPlayerName;
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

                            MojangPlayerName = JsonParser.parseString(in.lines().collect(Collectors.joining())).getAsJsonObject().get("name").getAsString();

                            SkinsRestorerAPI.getSkinStorage().setPlayerSkinData(MojangPlayer.getUniqueId(), MojangPlayerName, MojangPlayer.getSkinProperty(), System.currentTimeMillis());

                        }

                        PlayerSkin = SkinsRestorerAPI.getSkinStorage().findSkinData(MojangPlayerName);

                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

            SkinsRestorerAPI.getPlayerStorage().setSkinIdOfPlayer(Player.getUniqueId(), PlayerSkin.get().getIdentifier());

            try {
                SkinsRestorerAPI.getSkinApplier(Player.class).applySkin(Player);
            } catch (DataRequestException e) {
                throw new RuntimeException(e);
            }

            getLogger().info(" 玩家 " + Player.getName() + " 在进入服务器时设置 " + SkinName + "皮肤成功");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " 感谢您的使用，如果可以请给该项目点一个Star吧！！！期待您的下次使用");
    }
}
