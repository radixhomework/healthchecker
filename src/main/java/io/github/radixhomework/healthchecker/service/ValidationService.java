package io.github.radixhomework.healthchecker.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    public boolean validate(Object object, String objectName) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> constraintViolation = validator.validate(object);

        if (constraintViolation.isEmpty()) {
            return true;
        }

        log.error("Constraint violation detected while validating object: {}", objectName);
        log.error("Object content : {}", object.toString());
        log.error("Constraint violation found:");
        for (ConstraintViolation<Object> violation : constraintViolation) {
            log.error("  - {}", violation.getMessage());
        }
        return false;
    }
}
