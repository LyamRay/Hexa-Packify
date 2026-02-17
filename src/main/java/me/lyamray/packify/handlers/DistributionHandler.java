package me.lyamray.packify.handlers;
import lombok.extern.slf4j.Slf4j;
import me.lyamray.packify.PackifyConfig;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.UUID;

@Slf4j
public class DistributionHandler extends AbstractPackHandler {

    public DistributionHandler(JavaPlugin plugin, PackifyConfig config) {
        super(plugin, config);
    }

    @Override
    public void initialize() {}

    public void sendToPlayer(Player player, File zipFile, String url) {
        try {
            player.sendResourcePacks(buildRequest(zipFile, url));
            log.info("Pack sent to {}", player.getName());
        } catch (Exception e) {
            log.error("Failed to send pack to {}", player.getName(), e);
        }
    }

    public void sendToPlayerSync(Player player, File zipFile, String url) {
        Bukkit.getScheduler().runTask(plugin, () -> sendToPlayer(player, zipFile, url));
    }

    public void sendToAll(Collection<? extends Player> players, File zipFile, String url) {
        players.forEach(player -> sendToPlayerSync(player, zipFile, url));
    }

    private ResourcePackRequest buildRequest(File zipFile, String url) throws Exception {
        return ResourcePackRequest.resourcePackRequest()
                .packs(buildPackInfo(zipFile, url))
                .required(config.isRequired())
                .replace(config.isReplaceExisting())
                .build();
    }

    private ResourcePackInfo buildPackInfo(File zipFile, String url) throws Exception {
        return ResourcePackInfo.resourcePackInfo(
                UUID.nameUUIDFromBytes(url.getBytes()),
                new URI(url),
                sha1(zipFile)
        );
    }

    private String sha1(File file) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(file.toPath()));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-1: " + file, e);
        }
    }
}