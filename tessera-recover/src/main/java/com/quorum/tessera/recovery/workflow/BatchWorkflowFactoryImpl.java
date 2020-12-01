package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class BatchWorkflowFactoryImpl implements BatchWorkflowFactory {

    private Enclave enclave;

    private PayloadEncoder payloadEncoder;

    private Discovery discovery;

    private ResendBatchPublisher resendBatchPublisher;

    private long transactionCount;

    public void setEnclave(Enclave enclave) {
        this.enclave = enclave;
    }

    public void setPayloadEncoder(PayloadEncoder payloadEncoder) {
        this.payloadEncoder = payloadEncoder;
    }

    public void setDiscovery(Discovery discovery) {
        this.discovery = discovery;
    }

    public void setResendBatchPublisher(ResendBatchPublisher resendBatchPublisher) {
        this.resendBatchPublisher = resendBatchPublisher;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    @Override
    public BatchWorkflow create() {

        ValidateEnclaveStatus validateEnclaveStatus = new ValidateEnclaveStatus(enclave);
        DecodePayloadHandler decodePayloadHandler = new DecodePayloadHandler(payloadEncoder);
        PreparePayloadForRecipient preparePayloadForRecipient = new PreparePayloadForRecipient(payloadEncoder);
        FindRecipientFromPartyInfo findRecipientFromPartyInfo = new FindRecipientFromPartyInfo(discovery);
        FilterPayload filterPayload = new FilterPayload(enclave);
        SearchRecipientKeyForPayload searchRecipientKeyForPayload = new SearchRecipientKeyForPayload(enclave);
        SenderIsNotRecipient senderIsNotRecipient = new SenderIsNotRecipient(enclave);
        EncodedPayloadPublisher encodedPayloadPublisher = new EncodedPayloadPublisher(resendBatchPublisher);

        List<BatchWorkflowAction> handlers =
                List.of(
                        validateEnclaveStatus,
                        decodePayloadHandler,
                        filterPayload,
                        preparePayloadForRecipient,
                        searchRecipientKeyForPayload,
                        findRecipientFromPartyInfo,
                        senderIsNotRecipient,
                        encodedPayloadPublisher);

        return new BatchWorkflow() {

            private final AtomicLong filteredMessageCount = new AtomicLong(transactionCount);

            @Override
            public boolean execute(BatchWorkflowContext context) {

                context.setExpectedTotal(filteredMessageCount.get());

                boolean outcome =
                        handlers.stream().filter(Predicate.not(h -> h.execute(context))).findFirst().isEmpty();

                if (!outcome) {
                    context.setExpectedTotal(filteredMessageCount.decrementAndGet());
                    encodedPayloadPublisher.checkOutstandingPayloads(context);
                }
                return outcome;
            }

            @Override
            public long getPublishedMessageCount() {
                return encodedPayloadPublisher.getPublishedCount();
            }
        };
    }
}
