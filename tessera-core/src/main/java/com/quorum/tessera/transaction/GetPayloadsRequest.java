package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Objects;
import java.util.Optional;

public interface GetPayloadsRequest {

    MessageHash getTransactionHash();

    Optional<PublicKey> getRecipient();

    int getMaxNumber();

    long getStartTimeStamp();

    boolean isRaw();

    class Builder {

        private MessageHash messageHash;

        private PublicKey recipient;

        private int maxNumber;

        private long startTimeStamp;

        private boolean raw;

        public GetPayloadsRequest.Builder withRaw(boolean raw) {
            this.raw = raw;
            return this;
        }

        public GetPayloadsRequest.Builder withMaxNumber(int number) {
            this.maxNumber = number;
            return this;
        }

        public GetPayloadsRequest.Builder withRecipient(PublicKey recipient) {
            this.recipient = recipient;
            return this;
        }

        public GetPayloadsRequest.Builder withTransactionHash(MessageHash messageHash) {
            this.messageHash = messageHash;
            return this;
        }

        public GetPayloadsRequest.Builder withTransactionStartTime(long startTime) {
            this.startTimeStamp = startTime;
            return this;
        }

        public static GetPayloadsRequest.Builder create() {
            return new GetPayloadsRequest.Builder() {};
        }

        public GetPayloadsRequest build() {
            Objects.requireNonNull(messageHash,"Message hash is required");

            return new GetPayloadsRequest() {
                @Override
                public MessageHash getTransactionHash() {
                    return messageHash;
                }

                @Override
                public Optional<PublicKey> getRecipient() {
                    return Optional.ofNullable(recipient);
                }

                @Override
                public int getMaxNumber() {
                    return maxNumber;
                }

                @Override
                public long getStartTimeStamp() {
                    return startTimeStamp;
                }

                @Override
                public boolean isRaw() {
                    return raw;
                }
            };
        }

    }

}
