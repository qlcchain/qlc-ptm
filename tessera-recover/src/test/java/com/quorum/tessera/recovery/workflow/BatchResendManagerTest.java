package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.*;
import com.quorum.tessera.service.Service;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.util.Base64Codec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.AdditionalMatchers.gt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.lt;
import static org.mockito.Mockito.*;

public class BatchResendManagerTest {

    private PayloadEncoder payloadEncoder;

    private Enclave enclave;

    private TransactionManager resendStoreDelegate;

    private StagingEntityDAO stagingEntityDAO;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private Discovery discovery;

    private BatchResendManager manager;

    private ResendBatchPublisher resendBatchPublisher;

    private static final String KEY_STRING = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";

    private final PublicKey publicKey = PublicKey.from(Base64Codec.create().decode(KEY_STRING));

    @Before
    public void init() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);
        resendStoreDelegate = mock(TransactionManager.class);
        stagingEntityDAO = mock(StagingEntityDAO.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        discovery = mock(Discovery.class);
        resendBatchPublisher = mock(ResendBatchPublisher.class);

        manager =
                new BatchResendManagerImpl(
                        payloadEncoder,
                        Base64Codec.create(),
                        enclave,
                        stagingEntityDAO,
                        encryptedTransactionDAO,
                        discovery,
                        resendBatchPublisher,
                        5);

        when(enclave.status()).thenReturn(Service.Status.STARTED);

        final PublicKey publicKey =
                PublicKey.from(Base64Codec.create().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        MockBatchWorkflowFactory.resendBatchPublisher = resendBatchPublisher;
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(payloadEncoder);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(resendStoreDelegate);
        verifyNoMoreInteractions(stagingEntityDAO);
        verifyNoMoreInteractions(encryptedTransactionDAO);
        verifyNoMoreInteractions(discovery);
        verifyNoMoreInteractions(resendBatchPublisher);
        MockBatchWorkflowFactory.reset();
    }

    @Test
    public void resendBatch() {

        final ResendBatchRequest request =
                ResendBatchRequest.Builder.create().withBatchSize(3).withPublicKey(KEY_STRING).build();

        List<EncryptedTransaction> transactions =
                IntStream.range(0, 5)
                        .mapToObj(i -> mock(EncryptedTransaction.class))
                        .collect(Collectors.toUnmodifiableList());

        when(encryptedTransactionDAO.transactionCount()).thenReturn(101L);
        MockBatchWorkflowFactory.transactionCount = 101L;

        when(encryptedTransactionDAO.retrieveTransactions(lt(100), anyInt())).thenReturn(transactions);
        when(encryptedTransactionDAO.retrieveTransactions(gt(99), anyInt()))
                .thenReturn(singletonList(mock(EncryptedTransaction.class)));

        final BatchWorkflow batchWorkflow = MockBatchWorkflowFactory.getWorkflow();

        final ResendBatchResponse result = manager.resendBatch(request);

        assertThat(MockBatchWorkflowFactory.getExecuteInvocationCounter()).isEqualTo(101);
        assertThat(batchWorkflow.getPublishedMessageCount()).isEqualTo(101);

        assertThat(result.getTotal()).isEqualTo(101L);

        verify(encryptedTransactionDAO, times(21)).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO).transactionCount();
        verify(resendBatchPublisher, times(34)).publishBatch(any(), any());
    }

    @Test
    public void useMaxResultsWhenBatchSizeNotProvided() {

        final ResendBatchRequest request = ResendBatchRequest.Builder.create().withPublicKey(KEY_STRING).build();

        List<EncryptedTransaction> transactions =
                IntStream.range(0, 5)
                        .mapToObj(i -> mock(EncryptedTransaction.class))
                        .collect(Collectors.toUnmodifiableList());

        when(encryptedTransactionDAO.transactionCount()).thenReturn(101L);
        MockBatchWorkflowFactory.transactionCount = 101L;

        when(encryptedTransactionDAO.retrieveTransactions(lt(100), anyInt())).thenReturn(transactions);
        when(encryptedTransactionDAO.retrieveTransactions(gt(99), anyInt()))
                .thenReturn(singletonList(mock(EncryptedTransaction.class)));

        final BatchWorkflow batchWorkflow = MockBatchWorkflowFactory.getWorkflow();

        final ResendBatchResponse result = manager.resendBatch(request);

        assertThat(MockBatchWorkflowFactory.getExecuteInvocationCounter()).isEqualTo(101);
        assertThat(batchWorkflow.getPublishedMessageCount()).isEqualTo(101);

        assertThat(result.getTotal()).isEqualTo(101L);

        verify(encryptedTransactionDAO, times(21)).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO).transactionCount();
        verify(resendBatchPublisher, times(21)).publishBatch(any(), any());
    }

    @Test
    public void useMaxResultsAlsoWhenBatchSizeTooLarge() {

        final ResendBatchRequest request =
                ResendBatchRequest.Builder.create().withBatchSize(10000000).withPublicKey(KEY_STRING).build();

        List<EncryptedTransaction> transactions =
                IntStream.range(0, 5)
                        .mapToObj(i -> mock(EncryptedTransaction.class))
                        .collect(Collectors.toUnmodifiableList());

        when(encryptedTransactionDAO.transactionCount()).thenReturn(101L);
        MockBatchWorkflowFactory.transactionCount = 101L;

        when(encryptedTransactionDAO.retrieveTransactions(lt(100), anyInt())).thenReturn(transactions);
        when(encryptedTransactionDAO.retrieveTransactions(gt(99), anyInt()))
                .thenReturn(singletonList(mock(EncryptedTransaction.class)));

        final BatchWorkflow batchWorkflow = MockBatchWorkflowFactory.getWorkflow();

        final ResendBatchResponse result = manager.resendBatch(request);

        assertThat(MockBatchWorkflowFactory.getExecuteInvocationCounter()).isEqualTo(101);
        assertThat(batchWorkflow.getPublishedMessageCount()).isEqualTo(101);

        assertThat(result.getTotal()).isEqualTo(101L);

        verify(encryptedTransactionDAO, times(21)).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO).transactionCount();
        verify(resendBatchPublisher, times(21)).publishBatch(any(), any());
    }

    @Test
    public void createWithMinimalConstructor() {
        assertThat(
                        new BatchResendManagerImpl(
                                enclave, stagingEntityDAO, encryptedTransactionDAO, discovery, resendBatchPublisher, 1))
                .isNotNull();
    }

    @Test
    public void calculateBatchCount() {
        long numberOfRecords = 10;
        long maxResults = 3;

        int batchCount = BatchResendManagerImpl.calculateBatchCount(maxResults, numberOfRecords);

        assertThat(batchCount).isEqualTo(4);
    }

    @Test
    public void calculateBatchCountTotalLowerThanBatchSizeIsSingleBatch() {
        long numberOfRecords = 100;
        long maxResults = 10;

        int batchCount = BatchResendManagerImpl.calculateBatchCount(maxResults, numberOfRecords);

        assertThat(batchCount).isEqualTo(10);
    }

    @Test
    public void createBatchResendManager() {
        Config config = mock(Config.class);
        JdbcConfig jdbcConfig = mock(JdbcConfig.class);
        when(jdbcConfig.getUsername()).thenReturn("junit");
        when(jdbcConfig.getPassword()).thenReturn("");
        when(jdbcConfig.getUrl()).thenReturn("jdbc:h2:mem:test");
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);

        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        BatchResendManager result = BatchResendManager.create(config);

        assertThat(result).isNotNull();
    }

    @Test
    public void testStoreResendBatchMultipleVersions() {

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(publicKey)
                        .withCipherText("cipherText".getBytes())
                        .withCipherTextNonce(new Nonce("nonce".getBytes()))
                        .withRecipientBoxes(singletonList("box".getBytes()))
                        .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
                        .withRecipientKeys(singletonList(PublicKey.from("receiverKey".getBytes())))
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedContractTransactions(emptyMap())
                        .withExecHash(new byte[0])
                        .build();

        final byte[] raw = new PayloadEncoderImpl().encode(encodedPayload);

        PushBatchRequest request = PushBatchRequest.from(List.of(raw));

        StagingTransaction existing = new StagingTransaction();

        when(stagingEntityDAO.retrieveByHash(any())).thenReturn(Optional.of(existing));
        when(stagingEntityDAO.update(any(StagingTransaction.class))).thenReturn(new StagingTransaction());

        manager.storeResendBatch(request);

        verify(stagingEntityDAO).save(any(StagingTransaction.class));
    }
}
