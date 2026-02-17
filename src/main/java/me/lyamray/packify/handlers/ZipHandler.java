package me.lyamray.packify.handlers;

import me.lyamray.packify.PackifyConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHandler extends AbstractPackHandler {

    private final StructureHandler structureHandler;

    public ZipHandler(JavaPlugin plugin, PackifyConfig config, StructureHandler structureHandler) {
        super(plugin, config);
        this.structureHandler = structureHandler;
    }

    @Override
    public void initialize() {}

    public File zip() {
        File zipFile = resolveZipFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipPackRoot(zos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create zip", e);
        }
        return zipFile;
    }

    private void zipPackRoot(ZipOutputStream zos) throws IOException {
        Path root = structureHandler.getPackRoot().toPath();
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> addToZip(zos, root, path));
        }
    }

    private void addToZip(ZipOutputStream zos, Path root, Path file) {
        try {
            zos.putNextEntry(new ZipEntry(toZipEntry(root, file)));
            Files.copy(file, zos);
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("Failed to zip file: " + file, e);
        }
    }

    private String toZipEntry(Path root, Path file) {
        return root.relativize(file).toString().replace(File.separatorChar, '/');
    }

    private File resolveZipFile() {
        return new File(plugin.getDataFolder(), config.getPackName() + ".zip");
    }
}
