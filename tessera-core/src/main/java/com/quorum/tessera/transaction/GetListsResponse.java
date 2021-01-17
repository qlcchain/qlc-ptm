package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;

import java.util.*;

public interface GetListsResponse {

    List<byte[]> getUnencryptedTransactionDates();

    PrivacyMode getPrivacyMode();

    byte[] getExecHash();

    Set<MessageHash> getAffectedTransactions();

    Set<PublicKey> getManagedParties();

    PublicKey sender();

    int getPayloadsNumber();

    class Builder {

        private List<byte[]> unencryptedTransactionDates;

        private PrivacyMode privacyMode;

        private byte[] execHash = new byte[0];

        private Set<MessageHash> affectedTransactions = Collections.emptySet();

        private Set<PublicKey> managedParties = Collections.emptySet();

        private PublicKey sender;

        private int payloadsNumber;

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        public Builder withUnencryptedTransactionDates(List<byte[]> unencryptedTransactionDates) {
            this.unencryptedTransactionDates = unencryptedTransactionDates;
            return this;
        }

        public Builder withPrivacyMode(PrivacyMode privacyMode) {
            this.privacyMode = privacyMode;
            return this;
        }

        public Builder withExecHash(byte[] execHash) {
            this.execHash = execHash;
            return this;
        }

        public Builder withPayloadsNumber(int number) {
            this.payloadsNumber = number;
            return this;
        }

        public Builder withAffectedTransactions(Set<MessageHash> affectedTransactions) {
            this.affectedTransactions = affectedTransactions;
            return this;
        }

        public Builder withManagedParties(Set<PublicKey> managedKeys) {
            this.managedParties = managedKeys;
            return this;
        }

        public Builder withSender(PublicKey sender) {
            this.sender = sender;
            return this;
        }

        public GetListsResponse build() {

            Objects.requireNonNull(unencryptedTransactionDates, "unencrypted payload is required");
            Objects.requireNonNull(privacyMode, "Privacy mode is required");
            Objects.requireNonNull(sender, "transaction sender is required");

            if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                if (execHash.length == 0) {
                    throw new RuntimeException("ExecutionHash is required for PRIVATE_STATE_VALIDATION privacy mode");
                }
            }

            return new GetListsResponse() {

                @Override
                public List<byte[]> getUnencryptedTransactionDates() {
                    return unencryptedTransactionDates;
                }

                @Override
                public PrivacyMode getPrivacyMode() {
                    return privacyMode;
                }

                @Override
                public byte[] getExecHash() {
                    return Arrays.copyOf(execHash, execHash.length);
                }

                @Override
                public Set<MessageHash> getAffectedTransactions() {
                    return Set.copyOf(affectedTransactions);
                }

                @Override
                public Set<PublicKey> getManagedParties() {
                    return managedParties;
                }

                @Override
                public PublicKey sender() {
                    return sender;
                }

                @Override
                public int getPayloadsNumber() {
                    return payloadsNumber;
                }
            };
        }
    }
}
