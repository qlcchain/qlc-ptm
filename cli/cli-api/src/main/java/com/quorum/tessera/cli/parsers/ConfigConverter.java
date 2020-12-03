package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import com.quorum.tessera.encryption.KeyPair;

import picocli.CommandLine;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import java.io.FileWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigConverter implements CommandLine.ITypeConverter<Config> {
	public void configFileCreat(String cpath, String cfile, String pubkey, String prikey) throws Exception {
		File folder = new File(cpath);
		if (!folder.exists() && !folder.isDirectory()) {
			folder.mkdirs();
		}
		try (FileWriter fw = new FileWriter(cfile)) {
			JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(null);
			JsonObjectBuilder jsonSerConfigsBuilder = jsonBuilderFactory.createObjectBuilder();
			JsonObjectBuilder jsonJdbcObjectBuilder = jsonBuilderFactory.createObjectBuilder();
			jsonJdbcObjectBuilder.add("username", "qlcchain");
			jsonJdbcObjectBuilder.add("password", "");
			jsonJdbcObjectBuilder.add("url",
					"jdbc:h2:" + cpath + "/target/h2/tessera1;AUTO_SERVER=TRUE;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0");
			jsonJdbcObjectBuilder.add("autoCreateTables", true);
			JsonArrayBuilder jsonSerConfigsArrayBuilder = jsonBuilderFactory.createArrayBuilder();
			javax.json.JsonObject jsonSerConfigObject1 = jsonSerConfigsBuilder.add("app", "ThirdParty")
					.add("enabled", true).add("serverAddress", "http://localhost:9181")
					.add("bindingAddress", "http://127.0.0.1:9181").add("communicationType", "REST").build();
			javax.json.JsonObject jsonSerConfigObject2 = jsonSerConfigsBuilder.add("app", "Q2T").add("enabled", true)
					.add("serverAddress", "http://127.0.0.1:9182").add("communicationType", "REST").build();
			javax.json.JsonObject jsonSerConfigObject3 = jsonSerConfigsBuilder.add("app", "P2P").add("enabled", true)
					.add("serverAddress", "http://127.0.0.1:9183").add("bindingAddress", "http://0.0.0.0:9183")
					.add("communicationType", "REST").build();
			jsonSerConfigsArrayBuilder.add(jsonSerConfigObject1);
			jsonSerConfigsArrayBuilder.add(jsonSerConfigObject2);
			jsonSerConfigsArrayBuilder.add(jsonSerConfigObject3);
			JsonObjectBuilder jsonPeerObjectBuilder = jsonBuilderFactory.createObjectBuilder();
			JsonArrayBuilder jsonPeerArrayBuilder = jsonBuilderFactory.createArrayBuilder();
			javax.json.JsonObject jsonPeerObject1 = jsonPeerObjectBuilder.add("url", "http://localhost:9183").build();
			jsonPeerArrayBuilder.add(jsonPeerObject1);
			JsonObjectBuilder jsonKeyDataObjectBuilder = jsonBuilderFactory.createObjectBuilder();
			JsonArrayBuilder jsonKeyDataArrayBuilder = jsonBuilderFactory.createArrayBuilder();
			javax.json.JsonObject jsonKeyDataObject1 = jsonKeyDataObjectBuilder.add("privateKey", prikey)
					.add("publicKey", pubkey).build();
			jsonKeyDataArrayBuilder.add(jsonKeyDataObject1);
			jsonKeyDataObjectBuilder.add("keyData", jsonKeyDataArrayBuilder);
			JsonObjectBuilder jsonObjectBuilder = jsonBuilderFactory.createObjectBuilder();
			jsonObjectBuilder.add("useWhiteList", false);
			jsonObjectBuilder.add("jdbc", jsonJdbcObjectBuilder);
			jsonObjectBuilder.add("serverConfigs", jsonSerConfigsArrayBuilder);
			jsonObjectBuilder.add("peer", jsonPeerArrayBuilder);
			jsonObjectBuilder.add("keys", jsonKeyDataObjectBuilder);
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			String jsonString = gson.toJson(jsonObjectBuilder.build());
			fw.write(jsonString);
			fw.flush();
			fw.close();
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
