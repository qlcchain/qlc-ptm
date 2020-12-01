
package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.p2p.partyinfo.PartyInfoParserException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PartyInfoParserExceptionTest {

    @Test
    public void createWithMessage() {

        PartyInfoParserException result = new PartyInfoParserException("OUCH");

        assertThat(result).hasMessage("OUCH");

    }

}
