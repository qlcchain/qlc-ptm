package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfigsValidator implements ConstraintValidator<ValidServerConfigs, List<ServerConfig>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigsValidator.class);

    @Override
    public boolean isValid(List<ServerConfig> serverConfigs, ConstraintValidatorContext constraintContext) {
        if(serverConfigs == null) {
            return true;
        }

        final Map<AppType,Integer> counts = serverConfigs.stream()
            .collect(Collectors.toMap(ServerConfig::getApp,v -> 1,(l,r) -> l + 1));

        final int p2PEnabledConfigsCount = counts.getOrDefault(AppType.P2P,0);
        final int q2TEnabledConfigsCount = counts.getOrDefault(AppType.Q2T,0);

        if (p2PEnabledConfigsCount != 1) {
            LOGGER.debug("Only one P2P server must be configured and enabled.");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("Only one P2P server must be configured and enabled.")
                .addConstraintViolation();
            return false;
        }

        if (q2TEnabledConfigsCount == 0) {
            LOGGER.debug("At least one Q2T server must be configured and enabled.");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("At least one Q2T server must be configured and enabled.")
                .addConstraintViolation();
            return false;
        }

        return true;
    }
}
