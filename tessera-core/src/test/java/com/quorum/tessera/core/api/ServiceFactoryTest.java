package com.quorum.tessera.core.api;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ServiceFactoryTest {

    private MockServiceLocator mockServiceLocator;

    private ServiceFactoryImpl serviceFactory;

    @Before
    public void onSetUp() throws Exception {
        mockServiceLocator = (MockServiceLocator) ServiceLocator.create();
        Set services = new HashSet();
        services.add(mock(Config.class));
        services.add(mock(Enclave.class));
        services.add(mock(TransactionManager.class));
        services.add(mock(EncryptedTransactionDAO.class));
        services.add(mock(EncryptedRawTransactionDAO.class));
        services.add(mock(ResendManager.class));
        services.add(mock(PayloadPublisher.class));

        mockServiceLocator.setServices(services);

        serviceFactory = (ServiceFactoryImpl) ServiceFactory.create();
    }

    @Test
    public void transactionManager() {
        TransactionManager transactionManager = serviceFactory.transactionManager();
        assertThat(transactionManager).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void findNoServiceFoundThrowsIllegalState() {

        serviceFactory.find(NonExistentService.class);
    }

    static class NonExistentService {}

    @Test
    public void findConfig() {
        Config config = serviceFactory.config();
        assertThat(config).isNotNull();
    }
}
