package com.quorum.tessera.enclave;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityHashTest {
    @Test
    public void twoInstancesWithSameData() {

        byte[] someData = "SOMEDATA".getBytes();

        SecurityHash securityHash = SecurityHash.from(someData);

        SecurityHash anotherSecurityHash = SecurityHash.from(someData);

        assertThat(securityHash).isNotSameAs(anotherSecurityHash)
            .isEqualTo(anotherSecurityHash).hasSameHashCodeAs(anotherSecurityHash);

    }

    @Test
    public void equals() {
        EqualsVerifier.forClass(SecurityHash.class).verify();
    }

}
