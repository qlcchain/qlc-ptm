package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnclaveKeySynchroniserImpl implements EnclaveKeySynchroniser {

    private final Enclave enclave;

    private final NetworkStore networkStore;

    public EnclaveKeySynchroniserImpl(Enclave enclave, NetworkStore networkStore) {
        this.enclave = Objects.requireNonNull(enclave);
        this.networkStore = Objects.requireNonNull(networkStore);
    }

    @Override
    public void syncKeys() {

        NodeUri nodeUri = Optional.of(RuntimeContext.getInstance())
            .map(RuntimeContext::getP2pServerUri)
            .map(NodeUri::create).get();

        List<ActiveNode> activeNodes = networkStore.getActiveNodes()
            .filter(a -> a.getUri().equals(nodeUri))
            .collect(Collectors.toList());

        if(activeNodes.isEmpty()) {
            return;
        }

        final Set<PublicKey> storedKeys = activeNodes.stream()
            .flatMap(a -> a.getKeys().stream())
            .collect(Collectors.toSet());

        final Set<PublicKey> keys = enclave.getPublicKeys();

        if(!storedKeys.equals(keys)) {

            final Set<PublicKey> allKeys = Stream.concat(storedKeys.stream(),keys.stream())
                .collect(Collectors.toUnmodifiableSet());

            activeNodes.forEach(activeNode -> {
                ActiveNode modified = ActiveNode.Builder.from(activeNode)
                    .withKeys(allKeys)
                    .build();
                networkStore.store(modified);
            });
        }

    }
}
