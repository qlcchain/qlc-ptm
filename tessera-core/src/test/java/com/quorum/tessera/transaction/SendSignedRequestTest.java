package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SendSignedRequestTest {

    @Test
    public void build() {
        byte[] signedData = "SignedData".getBytes();
        List<PublicKey> recipients = List.of(mock(PublicKey.class));

        MessageHash affectedTransaction = mock(MessageHash.class);

        SendSignedRequest request =
                SendSignedRequest.Builder.create()
                        .withSender(mock(PublicKey.class))
                        .withSignedData(signedData)
                        .withExecHash("Exehash".getBytes())
                        .withRecipients(recipients)
                        .withAffectedContractTransactions(Set.of(affectedTransaction))
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .build();

        assertThat(request).isNotNull();
        assertThat(request.getAffectedContractTransactions()).containsOnly(affectedTransaction);
        assertThat(request.getSignedData()).containsExactly(signedData);
        assertThat(request.getExecHash()).containsExactly("Exehash".getBytes());
        assertThat(request.getRecipients()).hasSize(1).containsAll(recipients);
        assertThat(request.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    }

    @Test
    public void buildStandard() {
        byte[] signedData = "SignedData".getBytes();
        List<PublicKey> recipients = List.of(mock(PublicKey.class));

        SendSignedRequest request =
                SendSignedRequest.Builder.create()
                        .withSignedData(signedData)
                        .withRecipients(recipients)
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedContractTransactions(Collections.emptySet())
                        .withExecHash(new byte[0])
                        .build();

        assertThat(request).isNotNull();
        assertThat(request.getSignedData()).containsExactly(signedData);
        assertThat(request.getExecHash()).isEmpty();
        assertThat(request.getRecipients()).hasSize(1).containsAll(recipients);
        assertThat(request.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    }

    @Test(expected = NullPointerException.class)
    public void buidlwithNothing() {
        SendSignedRequest.Builder.create().build();
    }

    @Test(expected = NullPointerException.class)
    public void buidlWithoutSignedData() {
        SendSignedRequest.Builder.create().withRecipients(List.of(mock(PublicKey.class))).build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutRecipients() {
        SendSignedRequest.Builder.create().withSignedData("Data".getBytes()).build();
    }

    @Test(expected = RuntimeException.class)
    public void buildWithInvalidExecHash() {
        SendSignedRequest.Builder.create()
                .withSignedData("Data".getBytes())
                .withRecipients(Collections.emptyList())
                .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                .withAffectedContractTransactions(Collections.emptySet())
                .withExecHash(new byte[0])
                .build();
    }
}
