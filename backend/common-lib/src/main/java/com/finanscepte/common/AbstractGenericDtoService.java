package com.finanscepte.common;

import com.finanscepte.common.exception.ResourceNotFoundException;

import java.util.List;

public abstract class AbstractGenericDtoService<REQ, RES, E, ID> {

    protected abstract GenericRepository<E, ID> getRepository();

    protected abstract String getEntityName();

    protected abstract E toEntity(REQ request);

    protected abstract RES toResponse(E entity);

    protected abstract void applyUpdate(E entity, REQ request);

    public RES create(REQ request) {
        E saved = getRepository().save(toEntity(request));
        return toResponse(saved);
    }

    public List<RES> findAll() {
        return getRepository().findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RES findById(ID id) {
        return getRepository().findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), "id", id));
    }

    public RES update(ID id, REQ request) {
        E existing = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), "id", id));
        applyUpdate(existing, request);
        E updated = getRepository().save(existing);
        return toResponse(updated);
    }

    public void deleteById(ID id) {
        if (!getRepository().existsById(id)) {
            throw new ResourceNotFoundException(getEntityName(), "id", id);
        }
        getRepository().deleteById(id);
    }
}
