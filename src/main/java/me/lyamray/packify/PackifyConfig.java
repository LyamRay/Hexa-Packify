package me.lyamray.packify;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PackifyConfig {

    @Builder.Default
    private final String imageFolder = "images";

    @Builder.Default
    private final String packName = "Hexa-Studios Resourcepack";

    @Builder.Default
    private final String packDescription = "Powered by Hexa Studios";

    @Builder.Default
    private final int packFormat = 75;

    @Builder.Default
    private final boolean replaceExisting = false;

    @Builder.Default
    private final boolean required = true;
}