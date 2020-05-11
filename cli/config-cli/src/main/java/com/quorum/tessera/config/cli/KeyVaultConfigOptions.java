package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyVaultType;
import picocli.CommandLine;

import java.nio.file.Path;

public class KeyVaultConfigOptions {
    @CommandLine.Option(
            names = {"--vault.type", "-keygenvaulttype"},
            description =
                    "Specify the key vault provider the generated key is to be saved in.  If not set, the key will be encrypted and stored on the local filesystem.  Valid values: ${COMPLETION-CANDIDATES})")
    KeyVaultType vaultType;

    @CommandLine.Option(
            names = {"--vault.url", "-keygenvaulturl"},
            description = "Base url for key vault")
    String vaultUrl;

    @CommandLine.Option(
            names = {"--vault.hashicorp.approlepath", "-keygenvaultapprole"},
            description = "AppRole path for Hashicorp Vault authentication (defaults to 'approle')")
    String hashicorpApprolePath;

    @CommandLine.Option(
            names = {"--vault.hashicorp.secretenginepath", "-keygenvaultsecretengine"},
            description = "Name of already enabled Hashicorp v2 kv secret engine")
    String hashicorpSecretEnginePath;

    @CommandLine.Option(
            names = {"--vault.hashicorp.tlskeystore", "-keygenvaultkeystore"},
            description = "Path to JKS keystore for TLS Hashicorp Vault communication")
    Path hashicorpTlsKeystore;

    @CommandLine.Option(
            names = {"--vault.hashicorp.tlstruststore", "-keygenvaulttruststore"},
            description = "Path to JKS truststore for TLS Hashicorp Vault communication")
    Path hashicorpTlsTruststore;

    public KeyVaultType getVaultType() {
        return vaultType;
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getHashicorpApprolePath() {
        return hashicorpApprolePath;
    }

    public String getHashicorpSecretEnginePath() {
        return hashicorpSecretEnginePath;
    }

    public Path getHashicorpTlsKeystore() {
        return hashicorpTlsKeystore;
    }

    public Path getHashicorpTlsTruststore() {
        return hashicorpTlsTruststore;
    }
}
