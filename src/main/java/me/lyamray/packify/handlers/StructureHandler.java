package me.lyamray.packify.handlers;

import lombok.Getter;
import me.lyamray.packify.PackifyConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Getter
public class StructureHandler extends AbstractPackHandler {

    private final File packRoot;
    private final File itemsFolder;
    private final File modelsFolder;
    private final File texturesFolder;

    public StructureHandler(JavaPlugin plugin, PackifyConfig config) {
        super(plugin, config);
        this.packRoot      = new File(plugin.getDataFolder(), "texturepack");
        File assets        = new File(packRoot, "assets/minecraft");
        this.itemsFolder   = new File(assets, "items/item/paper");
        this.modelsFolder  = new File(assets, "models/item/paper");
        this.texturesFolder = new File(assets, "textures/item/paper");
    }

    @Override
    public void initialize() {
        createFolders();
    }

    public void build(Map<String, File> images) {
        createPackMeta();
        images.forEach((name, file) -> {
            writeItemDefinition(name);
            writeItemModel(name);
            copyTexture(name, file);
        });
    }

    private void createFolders() {
        createFolder(itemsFolder);
        createFolder(modelsFolder);
        createFolder(texturesFolder);
    }

    private void createFolder(File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + folder.getAbsolutePath());
        }
    }

    private void createPackMeta() {
        writeFile(new File(packRoot, "pack.mcmeta"), packMetaJson());
    }

    private void writeItemDefinition(String name) {
        writeFile(new File(itemsFolder, name + ".json"), itemDefinitionJson(name));
    }

    private void writeItemModel(String name) {
        writeFile(new File(modelsFolder, name + ".json"), itemModelJson(name));
    }

    private void copyTexture(String name, File source) {
        try {
            Files.copy(source.toPath(), new File(texturesFolder, name + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy texture: " + name, e);
        }
    }

    private void writeFile(File file, String content) {
        try {
            createFolder(file.getParentFile());
            Files.writeString(file.toPath(), content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + file, e);
        }
    }

    private String packMetaJson() {
        return """
                {
                  "pack": {
                    "description": "%s",
                    "pack_format": %d
                  }
                }
                """.formatted(config.getPackDescription(), config.getPackFormat());
    }

    private String itemDefinitionJson(String name) {
        return """
                {
                  "model": {
                    "type": "minecraft:model",
                    "model": "minecraft:item/paper/%s"
                  }
                }
                """.formatted(name);
    }

    private String itemModelJson(String name) {
        return """
                {
                  "parent": "item/handheld",
                  "textures": {
                    "layer0": "minecraft:item/paper/%s"
                  }
                }
                """.formatted(name);
    }
}
