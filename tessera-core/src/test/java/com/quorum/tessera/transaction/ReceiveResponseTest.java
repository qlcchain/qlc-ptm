package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceiveResponseTest {

    @Test
    public void fromSomeUnencryptedTransactionData() {
        byte[] someData = "SomeData".getBytes();
        ReceiveResponse result =
                ReceiveResponse.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withUnencryptedTransactionData(someData)
                        .withSender(PublicKey.from("sender".getBytes()))
                        .build();

        assertThat(result.getUnencryptedTransactionData()).containsExactly(someData);
        assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    }

    @Test(expected = RuntimeException.class)
    public void execHashRequiredFromPrivacyStateValidation() {
        byte[] someData = "SomeData".getBytes();
        ReceiveResponse.Builder.create()
                .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                .withUnencryptedTransactionData(someData)
                .withSender(PublicKey.from("sender".getBytes()))
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void privacyModeIsRequired() {
        byte[] someData = "SomeData".getBytes();
        ReceiveResponse.Builder.create()
                .withUnencryptedTransactionData(someData)
                .withSender(PublicKey.from("sender".getBytes()))
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void unencryptedTransactionDataIsRequired() {
        ReceiveResponse.Builder.create()
                .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                .withSender(PublicKey.from("sender".getBytes()))
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void senderIsRequired() {
        byte[] someData = "SomeData".getBytes();
        ReceiveResponse.Builder.create()
                .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                .withUnencryptedTransactionData(someData)
                .build();
    }
}
