package org.kxysl1k.serverStatus;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServerStatus extends JavaPlugin {

    private String statusChannelId;
    private String onlineChannelId;

    private List<String> loadingFrames;
    private int frameInterval;
    private int animationDuration;

    private String activeTitle;
    private String offlineTitle;
    private String onlineFormat;

    private BukkitTask animationTask;
    private BukkitTask onlineUpdateTask;

    private int frameIndex = 0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        // Регистрация слушателя остановки сервера
        getServer().getPluginManager().registerEvents(new ServerStopListener(this), this);

        // Задержка для асинхронной загрузки JDA
        Bukkit.getScheduler().runTaskLater(this, this::waitForJDA, 200L); // Задержка 10 секунд

        // Запуск анимации загрузки
        startLoadingAnimation();

        // Запуск обновления статуса онлайн
        startOnlineUpdater();
    }

    @Override
    public void onDisable() {
        stopTasks();
        CountDownLatch latch = new CountDownLatch(1);
        setChannelName(statusChannelId, offlineTitle, latch);
        try {
            latch.await(); // Ожидание завершения изменения имени канала
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();

        statusChannelId = config.getString("channels.status");
        onlineChannelId = config.getString("channels.online");

        loadingFrames = config.getStringList("animation.loading");
        frameInterval = config.getInt("animation.interval", 20);
        animationDuration = config.getInt("animation.duration", 120);

        activeTitle = config.getString("titles.active");
        offlineTitle = config.getString("titles.offline");
        onlineFormat = config.getString("titles.online_format");
    }

    private void waitForJDA() {
        // Проверка готовности JDA
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
            // Если JDA подключен, начать синхронизацию
            syncChannels();
        } else {
            // Если JDA не подключен, повторить через 5 секунд
            Bukkit.getScheduler().runTaskLater(this, this::waitForJDA, 100L); // Задержка 5 секунд
        }
    }

    private void startLoadingAnimation() {
        frameIndex = 0;

        // Задержка перед началом анимации (можно увеличить для стабильности)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            animationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                setChannelName(statusChannelId, loadingFrames.get(frameIndex), null);
                frameIndex = (frameIndex + 1) % loadingFrames.size();
            }, 0L, frameInterval);

            // Установить "Сервер работает" после завершения анимации
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (animationTask != null) animationTask.cancel();
                setChannelName(statusChannelId, activeTitle, null);
            }, animationDuration);

        }, 40L); // Можно попробовать увеличить задержку перед началом анимации, например, до 2 секунд
    }

    private void startOnlineUpdater() {
        onlineUpdateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            int online = Bukkit.getOnlinePlayers().size();
            int max = Bukkit.getMaxPlayers();
            String name = onlineFormat
                    .replace("{online}", String.valueOf(online))
                    .replace("{max}", String.valueOf(max));
            setChannelName(onlineChannelId, name, null);
        }, 0L, 20 * 10); // каждые 10 секунд
    }

    private void stopTasks() {
        if (animationTask != null) animationTask.cancel();
        if (onlineUpdateTask != null) onlineUpdateTask.cancel();
    }

    public void setChannelName(String channelId, String newName, CountDownLatch latch) {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda == null || channelId == null) {
            getLogger().warning("❗ JDA недоступен или ID канала не указан!");
            if (latch != null) latch.countDown();
            return;
        }

        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            getLogger().warning("❗ Канал с ID " + channelId + " не найден!");
            if (latch != null) latch.countDown();
            return;
        }
    }

    private void syncChannels() {
        getLogger().info("Начинаю синхронизацию каналов...");
        setChannelName(statusChannelId, activeTitle, null); // Установить активный статус во время синхронизации
        setChannelName(onlineChannelId, "Онлайн: " + Bukkit.getOnlinePlayers().size() + " из " + Bukkit.getMaxPlayers(), null);
        getLogger().info("Синхронизация завершена.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serverstatus.reload")) {
                sender.sendMessage("§cУ вас нет прав для использования этой команды.");
                return true;
            }

            reloadConfig();
            loadConfig();
            sender.sendMessage("§aКонфигурация успешно перезагружена.");
            getLogger().info(sender.getName() + " перезагрузил конфигурацию ServerStatusChannel.");

            stopTasks();
            startLoadingAnimation();
            startOnlineUpdater();
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("sync")) {
            if (!sender.hasPermission("serverstatus.sync")) {
                sender.sendMessage("§cУ вас нет прав для использования этой команды.");
                return true;
            }

            syncChannels();
            sender.sendMessage("§aКаналы успешно синхронизированы.");
            return true;
        }

        sender.sendMessage("§eИспользование: /serverstatus reload или /serverstatus sync");
        return true;
    }

    public String getStatusChannelId() {
        return statusChannelId;
    }

    public String getOfflineTitle() {
        return offlineTitle;
    }
}