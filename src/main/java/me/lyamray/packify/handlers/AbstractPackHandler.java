package me.lyamray.packify.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.lyamray.packify.PackifyConfig;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@RequiredArgsConstructor
public abstract class AbstractPackHandler implements IPackHandler {

    protected final JavaPlugin plugin;
    protected final PackifyConfig config;
}
