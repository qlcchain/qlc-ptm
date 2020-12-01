package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class EncodedPayloadManagerImplTest {

    private Enclave enclave;

    private PrivacyHelper privacyHelper;

    private MessageHashFactory messageHashFactory;

    private EncodedPayloadManager encodedPayloadManager;

    @Before
    public void init() {
        this.enclave = mock(Enclave.class);
        this.privacyHelper = mock(PrivacyHelper.class);
        this.messageHashFactory = mock(MessageHashFactory.class);

        this.encodedPayloadManager = new EncodedPayloadManagerImpl(enclave, privacyHelper, messageHashFactory);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(enclave, privacyHelper, messageHashFactory);
    }

    @Test
    public void createCallsEnclaveCorrectly() {
        final String testPayload = "test payload";

        final String senderKeyBase64 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final PublicKey sender = PublicKey.from(Base64.getDecoder().decode(senderKeyBase64));

        final String singleRecipientBase64 = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";
        final PublicKey singleRecipient = PublicKey.from(Base64.getDecoder().decode(singleRecipientBase64));
        final List<PublicKey> recipients = List.of(singleRecipient);

        final SendRequest request = SendRequest.Builder.create()
            .withSender(sender)
            .withRecipients(recipients)
            .withPayload(testPayload.getBytes())
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptySet())
            .withExecHash(new byte[0])
            .build();

        final EncodedPayload sampleReturnPayload = EncodedPayload.Builder.create().build();
        when(enclave.encryptPayload(any(), eq(sender), eq(List.of(singleRecipient, sender)), any(), any(), any())).thenReturn(sampleReturnPayload);

        final EncodedPayload encodedPayload = encodedPayloadManager.create(request);

        assertThat(encodedPayload).isEqualTo(sampleReturnPayload);

        verify(privacyHelper).findAffectedContractTransactionsFromSendRequest(emptySet());
        verify(privacyHelper).validateSendRequest(PrivacyMode.STANDARD_PRIVATE, List.of(singleRecipient, sender), emptyList());
        verify(enclave).encryptPayload(request.getPayload(), sender, List.of(singleRecipient, sender), PrivacyMode.STANDARD_PRIVATE, emptyList(), request.getExecHash());
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void createDeduplicatesRecipients() {
        final String testPayload = "test payload";

        final String senderKeyBase64 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final PublicKey sender = PublicKey.from(Base64.getDecoder().decode(senderKeyBase64));

        final String singleRecipientBase64 = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";
        final PublicKey singleRecipient = PublicKey.from(Base64.getDecoder().decode(singleRecipientBase64));
        final List<PublicKey> recipients = List.of(singleRecipient, sender, singleRecipient); //list the keys multiple times

        final SendRequest request = SendRequest.Builder.create()
            .withSender(sender)
            .withRecipients(recipients)
            .withPayload(testPayload.getBytes())
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptySet())
            .withExecHash(new byte[0])
            .build();

        final EncodedPayload sampleReturnPayload = EncodedPayload.Builder.create().build();
        when(enclave.encryptPayload(any(), eq(sender), eq(List.of(singleRecipient, sender)), any(), any(), any())).thenReturn(sampleReturnPayload);

        final EncodedPayload encodedPayload = encodedPayloadManager.create(request);

        assertThat(encodedPayload).isEqualTo(sampleReturnPayload);

        verify(privacyHelper).findAffectedContractTransactionsFromSendRequest(emptySet());
        verify(privacyHelper).validateSendRequest(PrivacyMode.STANDARD_PRIVATE, List.of(singleRecipient, sender), emptyList());
        verify(enclave).encryptPayload(request.getPayload(), sender, List.of(singleRecipient, sender), PrivacyMode.STANDARD_PRIVATE, emptyList(), request.getExecHash());
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void decryptTransactionSucceeds() {
        final String testPayload = "test payload";

        final String senderKeyBase64 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final PublicKey sender = PublicKey.from(Base64.getDecoder().decode(senderKeyBase64));

        final String singleRecipientBase64 = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";
        final PublicKey singleRecipient = PublicKey.from(Base64.getDecoder().decode(singleRecipientBase64));
        final List<PublicKey> recipients = List.of(singleRecipient); //list the keys multiple times

        final EncodedPayload samplePayload = EncodedPayload.Builder.create()
            .withSenderKey(sender)
            .withRecipientKeys(recipients)
            .withCipherText(testPayload.getBytes())
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash(new byte[0])
            .build();

        when(messageHashFactory.createFromCipherText(any())).thenReturn(new MessageHash("test hash".getBytes()));
        when(enclave.getPublicKeys()).thenReturn(Set.of(singleRecipient));
        when(enclave.unencryptTransaction(samplePayload, singleRecipient)).thenReturn("decrypted data".getBytes());

        final ReceiveResponse response = encodedPayloadManager.decrypt(samplePayload, null);

        assertThat(response.getUnencryptedTransactionData()).isEqualTo("decrypted data".getBytes());
        assertThat(response.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(response.getAffectedTransactions()).isEmpty();
        assertThat(response.getExecHash()).isEmpty();

        verify(messageHashFactory, times(2)).createFromCipherText(any());
        verify(enclave).getPublicKeys();
        verify(enclave, times(2)).unencryptTransaction(samplePayload, singleRecipient);
    }

    @Test
    public void decryptHasNoMatchingKeys() {
        final String testPayload = "test payload";

        final String senderKeyBase64 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final PublicKey sender = PublicKey.from(Base64.getDecoder().decode(senderKeyBase64));

        final String singleRecipientBase64 = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";
        final PublicKey singleRecipient = PublicKey.from(Base64.getDecoder().decode(singleRecipientBase64));
        final List<PublicKey> recipients = List.of(singleRecipient); //list the keys multiple times

        final EncodedPayload samplePayload = EncodedPayload.Builder.create()
            .withSenderKey(sender)
            .withRecipientKeys(recipients)
            .withCipherText(testPayload.getBytes())
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash(new byte[0])
            .build();

        when(messageHashFactory.createFromCipherText(any())).thenReturn(new MessageHash("test hash".getBytes()));
        when(enclave.getPublicKeys()).thenReturn(Set.of(singleRecipient));
        when(enclave.unencryptTransaction(any(), any())).thenThrow(new EnclaveException("test exception"));

        final Throwable throwable
            = catchThrowable(() -> encodedPayloadManager.decrypt(samplePayload, null));

        assertThat(throwable)
            .isInstanceOf(RecipientKeyNotFoundException.class)
            .hasMessage("No suitable recipient keys found to decrypt payload for dGVzdCBoYXNo");

        verify(messageHashFactory, times(2)).createFromCipherText(any());
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(any(), any());
    }

    @Test
    public void decryptBadCipherText() {
        final String testPayload = "test payload";

        final String senderKeyBase64 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final PublicKey sender = PublicKey.from(Base64.getDecoder().decode(senderKeyBase64));

        final String singleRecipientBase64 = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";
        final PublicKey singleRecipient = PublicKey.from(Base64.getDecoder().decode(singleRecipientBase64));
        final List<PublicKey> recipients = List.of(singleRecipient); //list the keys multiple times

        final EncodedPayload samplePayload = EncodedPayload.Builder.create()
            .withSenderKey(sender)
            .withRecipientKeys(recipients)
            .withCipherText(testPayload.getBytes())
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash(new byte[0])
            .build();

        when(messageHashFactory.createFromCipherText(any())).thenReturn(new MessageHash("test hash".getBytes()));
        when(enclave.getPublicKeys()).thenReturn(emptySet());
        when(enclave.unencryptTransaction(any(), any())).thenThrow(new EnclaveException("test exception"));

        final Throwable throwable
            = catchThrowable(() -> encodedPayloadManager.decrypt(samplePayload, singleRecipient));

        assertThat(throwable)
            .isInstanceOf(EnclaveException.class)
            .hasMessage("test exception");

        verify(messageHashFactory).createFromCipherText(any());
        verify(enclave).unencryptTransaction(samplePayload, singleRecipient);
    }

}
