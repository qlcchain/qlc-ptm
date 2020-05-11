package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.io.SystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ConfigFileUpdaterWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileUpdaterWriter.class);

    private final FilesDelegate filesDelegate;

    public ConfigFileUpdaterWriter(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

    public void updateAndWrite(
        List<KeyData> newKeys, KeyVaultConfig keyVaultConfig, Config config, Path configDest)
            throws IOException {
        LOGGER.info("Writing updated config to {}", configDest);

        update(newKeys, keyVaultConfig, config);

        try (OutputStream out = filesDelegate.newOutputStream(configDest, CREATE_NEW)) {
            JaxbUtil.marshal(config, out);
            LOGGER.info("Updated config written to {}", configDest);
        } catch (Exception e) {
            LOGGER.info("Writing updated config failed, cleaning up: deleting {}", configDest);
            filesDelegate.deleteIfExists(configDest);

            throw e;
        }
    }

    public void updateAndWriteToCLI(List<KeyData> newKeys, KeyVaultConfig keyVaultConfig, Config config) {
        LOGGER.info("Writing updated config to system out");
        update(newKeys, keyVaultConfig, config);
        JaxbUtil.marshal(config, SystemAdapter.INSTANCE.out());
        LOGGER.info("Updated config written to system out");
    }

    private void update(List<KeyData> newKeys, KeyVaultConfig keyVaultConfig, Config config) {

        config.getKeys().getKeyData().addAll(newKeys);
        if (Optional.ofNullable(keyVaultConfig).isPresent()
                && !Optional.ofNullable(config)
                        .map(Config::getKeys)
                        .flatMap(k -> k.getKeyVaultConfig(keyVaultConfig.getKeyVaultType()))
                        .isPresent()) {
            config.getKeys().addKeyVaultConfig(keyVaultConfig);
        }
    }
}
