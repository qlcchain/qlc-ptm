package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;

public interface PartyInfoServiceFactory {

    ResendManager resendManager();

    PartyInfoService partyInfoService();

    Enclave enclave();

    PayloadPublisher payloadPublisher();

    PartyInfoStore partyInfoStore();

    static PartyInfoServiceFactory create() {
        return new PartyInfoServiceFactoryImpl();
    }
}
