package com.finanscepte.common;

import com.finanscepte.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public abstract class AbstractGenericService<T, ID> implements GenericService<T, ID> {

    protected abstract GenericRepository<T, ID> getRepository();

    protected abstract String getEntityName();

    @Override
    public T save(T entity) {
        return getRepository().save(entity);
    }

    @Override
    public List<T> findAll() {
        return getRepository().findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    @Override
    public T update(ID id, T entity) {
        getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), "id", id));
        return getRepository().save(entity);
    }

    @Override
    public void deleteById(ID id) {
        getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), "id", id));
        getRepository().deleteById(id);
    }
}
