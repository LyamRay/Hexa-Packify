package me.lyamray.packify.handlers;

import lombok.extern.slf4j.Slf4j;
import me.lyamray.packify.PackifyConfig;
import okhttp3.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UploadHandler extends AbstractPackHandler {

    private static final String UPLOAD_URL = "https://fast-file.com/upload";
    private static final MediaType ZIP_TYPE = MediaType.parse("application/zip");

    private final OkHttpClient client;

    public UploadHandler(JavaPlugin plugin, PackifyConfig config) {
        super(plugin, config);
        this.client = buildClient();
    }

    @Override
    public void initialize() {}

    public String upload(File zipFile) {
        try (Response response = client.newCall(buildRequest(zipFile)).execute()) {
            return parseUrl(readBody(response));
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    private Request buildRequest(File zipFile) {
        return new Request.Builder()
                .url(UPLOAD_URL)
                .header("User-Agent", plugin.getName())
                .post(buildBody(zipFile))
                .build();
    }

    private RequestBody buildBody(File zipFile) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", zipFile.getName(), RequestBody.create(zipFile, ZIP_TYPE))
                .build();
    }

    private String readBody(Response response) throws IOException {
        if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
        if (response.body() == null) throw new IOException("Empty body");
        String body = response.body().string();
        if (body.isBlank()) throw new IOException("Blank body");
        return body;
    }

    private String parseUrl(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        if (!json.optBoolean("isSuccess")) throw new RuntimeException("Fast-File rejected upload: " + jsonStr);

        String fileId = json.getJSONArray("files").getJSONObject(0).getString("title");
        String link = "https://fast-file.com/" + fileId + "/download";
        log.info("Pack uploaded: {}", link);
        return link;
    }
}