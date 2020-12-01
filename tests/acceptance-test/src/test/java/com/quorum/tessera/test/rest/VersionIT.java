package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.Version;
import com.quorum.tessera.test.PartyHelper;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionIT {

    private final Client client = ClientBuilder.newClient();

    private PartyHelper partyHelper = PartyHelper.create();

    @Test
    public void getVersion() {

        final List<URI> allUris =
                partyHelper
                        .getParties()
                        .flatMap(p -> Stream.of(p.getQ2TUri(), p.getP2PUri()))
                        .collect(Collectors.toList());

        allUris.forEach(
                u -> {
                    final String version = client.target(u).path("/version").request().get(String.class);
                    assertThat(version).isEqualTo(Version.getVersion());
                });
    }

    @Test
    public void getDistributionVersion() {
        final List<URI> allUris =
                partyHelper
                        .getParties()
                        .flatMap(p -> Stream.of(p.getQ2TUri(), p.getP2PUri()))
                        .collect(Collectors.toList());

        allUris.forEach(
                u -> {
                    final String version = client.target(u).path("/version/distribution").request().get(String.class);
                    assertThat(version).isEqualTo(Version.getVersion());
                });

    }
  

    @Test
    public void getSupportedVersions() {

        List<URI> allUris = partyHelper.getParties().flatMap(p ->
            Stream.of(p.getQ2TUri(),p.getP2PUri())
        ).collect(Collectors.toList());

        allUris.forEach(u -> {

            JsonArray versions = client.target(u)
                .path("version")
                .path("api")
                .request().get(JsonArray.class);
            assertThat(versions.stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .toArray(String[]::new)).containsExactly("1.0", "2.0");

        });

    }

}



