package com.quorum.tessera.test.vault.aws;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.util.ElUtil;
import cucumber.api.java8.En;
import exec.NodeExecManager;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

public class AwsStepDefs implements En {

    private static final String AWS_SECRETS_MANAGER_URL = "/";
    private static final String AWS_REGION = "AWS_REGION";
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AtomicReference<Process> tesseraProcess = new AtomicReference<>();
    private final AtomicReference<WireMockServer> wireMockServer = new AtomicReference<>();

    private final String publicKey = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

    public AwsStepDefs() {

        Before(
                () -> {
                    //                        // only needed when running outside of maven build process
                    //                        System.setProperty(
                    //                            "application.jar",
                    //                            "path/to/tessera-app-VERSION.jar");
                });

        After(
                () -> {
                    if (wireMockServer.get() != null && wireMockServer.get().isRunning()) {
                        wireMockServer.get().stop();
                        System.out.println("Stopped WireMockServer...");
                    }

                    if (tesseraProcess.get() != null && tesseraProcess.get().isAlive()) {
                        tesseraProcess.get().destroy();
                        System.out.println("Stopped Tessera node...");
                    }
                });

        Given(
                "^the mock AWS Secrets Manager server has been started$",
                () -> {
                    final URL keystore = getClass().getResource("/certificates/localhost-with-san-keystore.jks");

                    // wiremock configures an HTTP server by default.  Even though we'll only use the HTTPS server we
                    // dynamically assign the HTTP port to ensure the default of 8080 is not used
                    wireMockServer.set(
                            new WireMockServer(
                                    options()
                                            .dynamicPort()
                                            .dynamicHttpsPort()
                                            .keystoreType("JKS")
                                            .keystorePath(keystore.getFile())
                                            .keystorePassword("testtest")
                                            .extensions(new ResponseTemplateTransformer(false))
                                    //                            .notifier(new ConsoleNotifier(true)) //enable to turn
                                    // on verbose debug msgs
                                    ));

                    wireMockServer.get().start();
                });

        Given(
                "^the mock AWS Secrets Manager server has stubs for the endpoints used to get secrets$",
                () -> {
                    wireMockServer
                            .get()
                            .stubFor(
                                    post(urlPathEqualTo(AWS_SECRETS_MANAGER_URL))
                                            .willReturn(
                                                    okJson(
                                                            String.format(
                                                                    "{\"ARN\": \"arn\",\n"
                                                                            + "   \"CreatedDate\": 121211444,\n"
                                                                            + "   \"Name\": \"publicKey\",\n"
                                                                            + "   \"SecretBinary\": null,\n"
                                                                            + "   \"SecretString\": \"%s\",\n"
                                                                            + "   \"VersionId\": \"123\",\n"
                                                                            + "   \"VersionStages\": [ \"stage1\" ]\n"
                                                                            + "}",
                                                                    publicKey))));
                });

        When(
                "^Tessera is started with the correct AWS Secrets Manager environment variables$",
                () -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("awsSecretsManagerEndpoint", wireMockServer.get().baseUrl());

                    Path tempTesseraConfig =
                            ElUtil.createTempFileFromTemplate(
                                    getClass().getResource("/vault/tessera-aws-config.json"), params);
                    tempTesseraConfig.toFile().deleteOnExit();

                    final String jarfile = System.getProperty("application.jar");

                    final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-node.xml");
                    Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pidA.pid");

                    final URL truststore = getClass().getResource("/certificates/truststore.jks");

                    List<String> args =
                            new ArrayList<>(
                                    Arrays.asList(
                                            "java",
                                            // we set the truststore so that Tessera can trust the wiremock server
                                            "-Djavax.net.ssl.trustStore=" + truststore.getFile(),
                                            "-Djavax.net.ssl.trustStorePassword=testtest",
                                            "-Dspring.profiles.active=disable-unixsocket",
                                            "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                                            "-Daws.region=a-region",
                                            "-Daws.accessKeyId=an-id",
                                            "-Daws.secretAccessKey=a-key",
                                            "-Ddebug=true",
                                            "-jar",
                                            jarfile,
                                            "-configfile",
                                            tempTesseraConfig.toString(),
                                            "-pidfile",
                                            pid.toAbsolutePath().toString(),
                                            "-jdbc.autoCreateTables",
                                            "true"));

                    startTessera(args, tempTesseraConfig);
                });

        Then(
                "^Tessera will retrieve the key pair from AWS Secrets Manager$",
                () -> {
                    wireMockServer.get().verify(4, postRequestedFor(urlEqualTo(AWS_SECRETS_MANAGER_URL)));

                    final URL partyInfoUrl =
                            UriBuilder.fromUri("http://localhost").port(8080).path("partyinfo").build().toURL();

                    HttpURLConnection partyInfoUrlConnection = (HttpURLConnection) partyInfoUrl.openConnection();
                    partyInfoUrlConnection.connect();

                    int partyInfoResponseCode = partyInfoUrlConnection.getResponseCode();
                    assertThat(partyInfoResponseCode).isEqualTo(HttpURLConnection.HTTP_OK);

                    JsonReader jsonReader = Json.createReader(partyInfoUrlConnection.getInputStream());

                    JsonObject partyInfoObject = jsonReader.readObject();

                    assertThat(partyInfoObject).isNotNull();
                    assertThat(partyInfoObject.getJsonArray("keys")).hasSize(1);
                    assertThat(partyInfoObject.getJsonArray("keys").getJsonObject(0).getString("key"))
                            .isEqualTo(publicKey);
                });

        Given(
                "^the mock AWS Secrets Manager server has stubs for the endpoints used to store secrets$",
                () -> {
                    wireMockServer
                            .get()
                            .stubFor(
                                    post(urlPathEqualTo(AWS_SECRETS_MANAGER_URL))
                                            .willReturn(
                                                    okJson(
                                                            ("{\n"
                                                                    + "   \"ARN\": \"string\",\n"
                                                                    + "   \"Name\": \"string\",\n"
                                                                    + "   \"VersionId\": \"string\"\n"
                                                                    + "}"))));
                });

        When(
                "^Tessera keygen is run with the following CLI args and AWS Secrets Manager environment variables$",
                (String cliArgs) -> {
                    final String jarfile = System.getProperty("application.jar");

                    final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-test.xml");

                    final URL truststore = getClass().getResource("/certificates/truststore.jks");

                    String formattedArgs = String.format(cliArgs, wireMockServer.get().baseUrl());

                    List<String> args = new ArrayList<>();
                    args.addAll(
                            Arrays.asList(
                                    "java",
                                    // we set the truststore so that Tessera can trust the wiremock server
                                    "-Djavax.net.ssl.trustStore=" + truststore.getFile(),
                                    "-Djavax.net.ssl.trustStorePassword=testtest",
                                    "-Dspring.profiles.active=disable-unixsocket",
                                    "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                                    "-Ddebug=true",
                                    "-Daws.region=a-region",
                                    "-Daws.accessKeyId=an-id",
                                    "-Daws.secretAccessKey=a-key",
                                    "-jar",
                                    jarfile));
                    args.addAll(Arrays.asList(formattedArgs.split(" ")));

                    startTessera(args, null); // node is not started during keygen so do not want to verify
                });

        Then(
                "^key pairs nodeA and nodeB will have been added to the AWS Secrets Manager$",
                () -> {
                    wireMockServer.get().verify(4, postRequestedFor(urlEqualTo(AWS_SECRETS_MANAGER_URL)));
                });
    }

    private void startTessera(List<String> args, Path verifyConfig) throws Exception {
        System.out.println(String.join(" ", args));

        ProcessBuilder tesseraProcessBuilder = new ProcessBuilder(args);

        Map<String, String> tesseraEnvironment = tesseraProcessBuilder.environment();
        tesseraEnvironment.put(AWS_REGION, "us-east-1");

        try {
            tesseraProcess.set(tesseraProcessBuilder.redirectErrorStream(true).start());
        } catch (NullPointerException ex) {
            throw new NullPointerException("Check that application.jar property has been set");
        }

        executorService.submit(
                () -> {
                    try (BufferedReader reader =
                            Stream.of(tesseraProcess.get().getInputStream())
                                    .map(InputStreamReader::new)
                                    .map(BufferedReader::new)
                                    .findAny()
                                    .get()) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }

                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });

        CountDownLatch startUpLatch = new CountDownLatch(1);

        if (Objects.nonNull(verifyConfig)) {
            final Config config = JaxbUtil.unmarshal(Files.newInputStream(verifyConfig), Config.class);

            final URL bindingUrl =
                    UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri()).path("upcheck").build().toURL();

            executorService.submit(
                    () -> {
                        while (true) {
                            try {
                                HttpURLConnection conn = (HttpURLConnection) bindingUrl.openConnection();
                                conn.connect();

                                System.out.println(bindingUrl + " started." + conn.getResponseCode());

                                startUpLatch.countDown();
                                return;
                            } catch (IOException ex) {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(200L);
                                } catch (InterruptedException ex1) {
                                }
                            }
                        }
                    });

            boolean started = startUpLatch.await(30, TimeUnit.SECONDS);

            if (!started) {
                System.err.println(bindingUrl + " Not started. ");
            }
        }

        executorService.submit(
                () -> {
                    try {
                        int exitCode = tesseraProcess.get().waitFor();
                        startUpLatch.countDown();
                        if (0 != exitCode) {
                            System.err.println("Tessera node exited with code " + exitCode);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });

        startUpLatch.await(30, TimeUnit.SECONDS);
    }
}
