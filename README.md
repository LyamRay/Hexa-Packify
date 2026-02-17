# Hexa-Packify

A lightweight Java library for Minecraft Paper plugins that handles resource pack generation and distribution. Drop images into a folder, and Packify takes care of the rest — building the pack structure, zipping it, uploading it, and sending it to players.

Built and used internally at [Hexa Studios](https://hexastudios.net).

---

## How It Works

1. Place PNG images in your plugin's image folder
2. Call `initialize()` — Packify scans the folder, generates the resource pack structure, zips it, and uploads it to Fast-File
3. Send the pack to players on join

---

## Requirements

- Paper 1.21+
- Java 21+
- Lombok (annotation processor in your plugin project)

OkHttp3 and org.json are shaded into the jar — you do not need to add them yourself.

---

## Installation

Add JitPack to your repositories and Packify as a dependency.

**build.gradle**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
}

dependencies {
    implementation 'com.github.lyamray:Hexa-Packify:1.0.0'
    compileOnly 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'
}
```

Replace `1.0.0` with the version tag you want from the [releases page](https://github.com/lyamray/Hexa-Packify/releases).

---

## Usage

### Setup

Initialize Packify in your plugin's `onEnable`. It runs asynchronously and returns a `CompletableFuture<Packify>` so you can act once the pack is ready.

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PackifyConfig config = PackifyConfig.builder()
                .packName(getName())
                .build();

        Packify.create(this, config)
                .initialize()
                .thenAccept(packify -> getLogger().info("Pack ready: " + packify.getPackUrl()))
                .exceptionally(err -> {
                    getLogger().severe("Packify failed: " + err.getMessage());
                    return null;
                });
    }
}
```

### Sending the Pack to Players

```java
// From an async context
Packify.get().sendToPlayer(player);

// From a sync event handler
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Packify.get().sendToPlayerSync(event.getPlayer());
}

// Send to everyone online
Packify.get().sendToAll(Bukkit.getOnlinePlayers());
```

### Creating Items

Use `ITEM_MODEL` with a `NamespacedKey` pointing to the image by its filename (without the `.png` extension).

```java
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public ItemStack createItem(String imageId) {
    if (imageId == null) return new ItemStack(Material.AIR);

    ItemStack item = new ItemStack(Material.PAPER);
    item.setData(DataComponentTypes.ITEM_MODEL, new NamespacedKey("minecraft", "item/paper/" + imageId));
    return item;
}
```

If the image file is `banner1.png`, the `imageId` is `banner1`.

### Checking Loaded Images

```java
Packify.get().getImages().keySet().forEach(System.out::println);
```

### Reuploading

Fast-File links expire. Call `reupload()` to get a fresh URL and push it back to online players.

```java
Packify.get().reupload().thenAccept(packify ->
        Bukkit.getOnlinePlayers().forEach(packify::sendToPlayerSync)
);
```

---

## Configuration

```groovy
PackifyConfig config = PackifyConfig.builder()
        .imageFolder("images")           // subfolder in your plugin data folder  — default: "images"
        .packName("MyPack")              // used as the zip filename              — default: "CustomPack"
        .packDescription("My Pack")      // shown in Minecraft's pack screen      — default: "Custom Resource Pack"
        .packFormat(75)                  // resource pack format version           — default: 75 (1.21.11)
        .replaceExisting(false)          // replace other active server packs     — default: false
        .required(true)                  // disconnect players who decline         — default: true
        .build();
```

### Pack Format Reference

| Minecraft version | Pack format |
|-------------------|-------------|
| 1.21.11           | 75          |
| 1.21.9 – 1.21.10  | 69.0        |
| 1.21.7 – 1.21.8   | 64          |
| 1.21.6            | 63          |

---

## Folder Structure

At runtime your plugin folder will look like this:

```
plugins/YourPlugin/
├── images/           ← put your PNG files here
│   ├── banner1.png
│   └── logo.png
├── texturepack/      ← generated, do not edit
└── MyPack.zip        ← generated, uploaded automatically
```

---

## API Structure

```
me.hexastudios.packify
├── Packify               Main entry point — create(), get(), initialize(), sendToPlayer(), ...
├── PackifyConfig         Builder-based config

me.hexastudios.packify.handler
├── IPackHandler          Interface all handlers implement
├── AbstractPackHandler   Base class — holds plugin + config references
├── ImageHandler          Scans the image folder, collects PNG files
├── StructureHandler      Generates the pack.mcmeta, item definitions, models, and copies textures
├── ZipHandler            Zips the generated texturepack folder
├── UploadHandler         Uploads the zip to Fast-File.com and returns the download URL
└── DistributionHandler   Sends the resource pack to players with SHA-1 verification
```

---

## Notes

- Images are collected once on `initialize()`. To pick up new files, restart the plugin or call `initialize()` again.
- Filenames are sanitized automatically — spaces become dashes, special characters are stripped, everything is lowercased.
- The pack is uploaded to [Fast-File.com](https://fast-file.com). Links expire over time — use `reupload()` to refresh.
- With `required(true)`, players who decline the pack prompt will be disconnected.
- With `replaceExisting(false)`, your pack stacks alongside any existing server resource pack instead of replacing it.

---

## License

Free to use in any project. Credit appreciated but not required.

---

Made by [LyamRay](https://github.com/lyamray) @ [Hexa Studios](https://hexastudios.net)
