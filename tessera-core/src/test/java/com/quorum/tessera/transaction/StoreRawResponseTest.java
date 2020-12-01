package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StoreRawResponseTest {

    @Test
    public void createFromTransactionHash() {
        MessageHash transactionHash = mock(MessageHash.class);
        StoreRawResponse result = StoreRawResponse.from(transactionHash);

        assertThat(result).isNotNull();
        assertThat(result.getHash()).isSameAs(transactionHash);
    }

    @Test(expected = NullPointerException.class)
    public void buildWithNullHash() {
        StoreRawResponse.from(null);
    }

}
