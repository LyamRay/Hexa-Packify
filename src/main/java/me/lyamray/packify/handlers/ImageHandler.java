package me.lyamray.packify.handlers;

import me.lyamray.packify.PackifyConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImageHandler extends AbstractPackHandler {

    private Map<String, File> images = new HashMap<>();

    private final File imageFolder;

    public ImageHandler(JavaPlugin plugin, PackifyConfig config) {
        super(plugin, config);
        this.imageFolder = resolveImageFolder();
    }

    @Override
    public void initialize() {
        ensureFolder();
        images = collectImages();
    }

    public Map<String, File> getImages() {
        return Map.copyOf(images);
    }

    private File resolveImageFolder() {
        return new File(plugin.getDataFolder(), config.getImageFolder());
    }

    private void ensureFolder() {
        if (!imageFolder.exists() && !imageFolder.mkdirs()) {
            throw new RuntimeException("Failed to create image folder: " + imageFolder.getAbsolutePath());
        }
    }

    private Map<String, File> collectImages() {
        return Optional.ofNullable(imageFolder.listFiles(this::isPng))
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(
                        file -> sanitize(stripExtension(file.getName())),
                        file -> file,
                        (a, b) -> b
                ));
    }

    private boolean isPng(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".png");
    }

    private String stripExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(0, i) : filename;
    }

    private String sanitize(String name) {
        return name.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9-_]", "");
    }
}