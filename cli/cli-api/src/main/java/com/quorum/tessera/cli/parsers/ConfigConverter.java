package com.quorum.tessera.cli.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import com.quorum.tessera.encryption.KeyPair;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigConverter implements CommandLine.ITypeConverter<Config> {
    public void configFileCreat(String cpath, String cfile, String pubkey, String prikey) throws Exception {
        File folder = new File(cpath);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        try (FileWriter fw = new FileWriter(cfile)) {
            JsonObject jsonJdbcObject = new JsonObject();
            jsonJdbcObject.addProperty("username", "qlcchain");
            jsonJdbcObject.addProperty("password", "");
            jsonJdbcObject.addProperty("url",
                    "jdbc:h2:" + cpath + "/target/h2/tessera1;AUTO_SERVER=TRUE;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0");
            jsonJdbcObject.addProperty("autoCreateTables", true);

            JsonArray jsonSerConfigsArray = new JsonArray();

            JsonObject jsonSerConfigObject1 = new JsonObject();
            jsonSerConfigObject1.addProperty("app", "ThirdParty");
            jsonSerConfigObject1.addProperty("enabled", true);
            jsonSerConfigObject1.addProperty("serverAddress", "http://localhost:9181");
            jsonSerConfigObject1.addProperty("bindingAddress", "http://127.0.0.1:9181");
            jsonSerConfigObject1.addProperty("communicationType", "REST");
            jsonSerConfigsArray.add(jsonSerConfigObject1);

            JsonObject jsonSerConfigObject2 = new JsonObject();
            jsonSerConfigObject2.addProperty("app", "Q2T");
            jsonSerConfigObject2.addProperty("enabled", true);
            jsonSerConfigObject2.addProperty("serverAddress", "http://127.0.0.1:9182");
            jsonSerConfigObject2.addProperty("communicationType", "REST");
            jsonSerConfigsArray.add(jsonSerConfigObject2);

            JsonObject jsonSerConfigObject3 = new JsonObject();
            jsonSerConfigObject3.addProperty("app", "P2P");
            jsonSerConfigObject3.addProperty("enabled", true);
            jsonSerConfigObject3.addProperty("serverAddress", "http://127.0.0.1:9183");
            jsonSerConfigObject3.addProperty("bindingAddress", "http://0.0.0.0:9183");
            JsonObject jsonSerConfigTls = new JsonObject();
            jsonSerConfigTls.addProperty("tls", "OFF");
            jsonSerConfigObject3.add("sslConfig", jsonSerConfigTls);
            jsonSerConfigObject3.addProperty("communicationType", "REST");
            jsonSerConfigsArray.add(jsonSerConfigObject3);

            JsonArray jsonPeerArray = new JsonArray();
            JsonObject jsonPeerObject = new JsonObject();
            jsonPeerObject.addProperty("url", "http://localhost:9183");
            jsonPeerArray.add(jsonPeerObject);

            JsonArray jsonKeyDataArray = new JsonArray();
            JsonObject jsonKeyDataObject1 = new JsonObject();
            jsonKeyDataObject1.addProperty("privateKey", prikey);
            jsonKeyDataObject1.addProperty("publicKey", pubkey);
            jsonKeyDataArray.add(jsonKeyDataObject1);
            JsonObject jsonKeyDataObject = new JsonObject();
            jsonKeyDataObject.add("keyData", jsonKeyDataArray);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("useWhiteList", false);
            jsonObject.add("jdbc", jsonJdbcObject);
            jsonObject.add("serverConfigs", jsonSerConfigsArray);
            jsonObject.add("peer", jsonPeerArray);
            jsonObject.add("keys", jsonKeyDataObject);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            String jsonString = gson.toJson(jsonObject);
            fw.write(jsonString);
            fw.flush();
        }
    }

    @Override
    public Config convert(final String value) throws Exception {
        final ConfigFactory configFactory = ConfigFactory.create();

        final Path path = Paths.get(value);

        if (!Files.exists(path)) {
            Encryptor encryptor = EncryptorFactory.newFactory("NACL").create();
            KeyPair keypair = encryptor.generateNewKeys();
            configFileCreat(path.getParent().toString(), path.toString(), keypair.getPublicKey().encodeToBase64(),
                    keypair.getPrivateKey().encodeToBase64());
            // throw new FileNotFoundException(String.format("%s not found.", path));
        }

        ConfigFileStore.create(path);

        try (InputStream in = Files.newInputStream(path)) {
            return configFactory.create(in);
        }
    }
}
