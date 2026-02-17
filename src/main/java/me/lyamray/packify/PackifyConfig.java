package me.lyamray.packify;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PackifyConfig {

    @Builder.Default
    private final String imageFolder = "images";

    @Builder.Default
    private final String packName = "CustomPack";

    @Builder.Default
    private final String packDescription = "Custom Resource Pack";

    @Builder.Default
    private final int packFormat = 34;

    @Builder.Default
    private final boolean replaceExisting = false;

    @Builder.Default
    private final boolean required = true;
}