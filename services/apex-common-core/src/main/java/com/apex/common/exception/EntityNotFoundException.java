package com.apex.common.exception;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s with identifier [%s] was not found.", entityName, id), "APEX_ENTITY_NOT_FOUND");
    }
}

