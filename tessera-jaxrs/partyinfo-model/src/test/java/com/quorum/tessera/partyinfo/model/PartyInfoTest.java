package com.quorum.tessera.partyinfo.model;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PartyInfoTest {

    @Test
    public void createSimple() {
        String url = "someurl";
        Set<Recipient> recipients = Set.of(mock(Recipient.class));
        Set<Party> parties = Set.of(mock(Party.class));

        PartyInfo partyInfo = new PartyInfo(url,recipients,parties);

        assertThat(partyInfo.getParties()).isEqualTo(parties);
        assertThat(partyInfo.getRecipients()).isEqualTo(recipients);
        assertThat(partyInfo.getUrl()).isSameAs(url);

    }

}
