package me.lyamray.packify;

import lombok.Getter;
import me.lyamray.packify.handlers.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public class Packify {

    private static Packify instance;

    private final ImageHandler imageHandler;
    private final StructureHandler structureHandler;
    private final ZipHandler zipHandler;
    private final UploadHandler uploadHandler;
    private final DistributionHandler distributionHandler;

    private File zipFile;
    private String packUrl;
    private boolean ready = false;

    public static Packify create(JavaPlugin plugin, PackifyConfig config) {
        if (instance != null) throw new IllegalStateException("Packify already initialized!");
        instance = new Packify(plugin, config);
        return instance;
    }

    public static Packify get() {
        if (instance == null) throw new IllegalStateException("Packify not initialized! Call Packify.create() first.");
        return instance;
    }

    private Packify(JavaPlugin plugin, PackifyConfig config) {
        this.imageHandler       = new ImageHandler(plugin, config);
        this.structureHandler   = new StructureHandler(plugin, config);
        this.zipHandler         = new ZipHandler(plugin, config, structureHandler);
        this.uploadHandler      = new UploadHandler(plugin, config);
        this.distributionHandler = new DistributionHandler(plugin, config);
    }

    public CompletableFuture<Packify> initialize() {
        return CompletableFuture.supplyAsync(this::build);
    }

    public void sendToPlayer(Player player) {
        assertReady();
        distributionHandler.sendToPlayer(player, zipFile, packUrl);
    }

    public void sendToPlayerSync(Player player) {
        assertReady();
        distributionHandler.sendToPlayerSync(player, zipFile, packUrl);
    }

    public void sendToAll(Collection<? extends Player> players) {
        assertReady();
        distributionHandler.sendToAll(players, zipFile, packUrl);
    }

    public CompletableFuture<Packify> reupload() {
        return CompletableFuture.supplyAsync(() -> {
            packUrl = uploadHandler.upload(zipFile);
            return this;
        });
    }

    public Map<String, File> getImages() {
        return imageHandler.getImages();
    }

    private Packify build() {
        imageHandler.initialize();
        structureHandler.initialize();
        structureHandler.build(imageHandler.getImages());
        zipFile = zipHandler.zip();
        packUrl = uploadHandler.upload(zipFile);
        ready = true;
        return this;
    }

    private void assertReady() {
        if (!ready) throw new IllegalStateException("Packify is not ready! Call initialize() first.");
    }
}