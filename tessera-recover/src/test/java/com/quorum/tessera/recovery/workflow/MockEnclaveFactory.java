package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

import static org.mockito.Mockito.mock;

public class MockEnclaveFactory implements EnclaveFactory {
    @Override
    public Enclave create(Config config) {
        return mock(Enclave.class);
    }
}
