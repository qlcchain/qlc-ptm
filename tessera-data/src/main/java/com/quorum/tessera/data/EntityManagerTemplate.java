package com.quorum.tessera.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Objects;

public class EntityManagerTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerTemplate.class);

    private EntityManagerFactory entityManagerFactory;

    public EntityManagerTemplate(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
    }

    public <T> T execute(EntityManagerCallback<T> callback) {
        LOGGER.debug("Enter callback");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            T outcome = callback.execute(entityManager);
            transaction.commit();
            LOGGER.debug("JPA callback success {}", outcome);
            return outcome;
        } catch (Exception ex) {
            LOGGER.warn("JPA exception thrown during execution {}", ex.getMessage());
            LOGGER.debug("", ex);
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }
}
