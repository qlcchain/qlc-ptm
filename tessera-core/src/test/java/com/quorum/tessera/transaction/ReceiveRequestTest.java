package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ReceiveRequestTest {

    @Test(expected = NullPointerException.class)
    public void buildWithNothing() {
        ReceiveRequest.Builder.create().build();
    }

    @Test
    public void buildWithTransactionHash() {
        MessageHash messageHash = mock(MessageHash.class);
        ReceiveRequest result = ReceiveRequest.Builder.create()
            .withTransactionHash(messageHash)
            .build();

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isNotNull().isSameAs(messageHash);
        assertThat(result.getRecipient()).isNotPresent();
    }

    @Test(expected = NullPointerException.class)
    public void buildOnlyWithRecipient() {
        PublicKey recipient = mock(PublicKey.class);
        ReceiveRequest.Builder.create()
            .withRecipient(recipient)
            .build();
    }

    @Test
    public void buildWithTransactionHashAndRecipent() {
        MessageHash messageHash = mock(MessageHash.class);
        PublicKey recipient = mock(PublicKey.class);
        ReceiveRequest result = ReceiveRequest.Builder.create()
            .withTransactionHash(messageHash)
            .withRecipient(recipient)
            .build();

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isNotNull().isSameAs(messageHash);
        assertThat(result.getRecipient()).containsSame(recipient);
    }
}
