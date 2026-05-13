package com.finanscepte.common;

import java.util.List;
import java.util.Optional;

public interface GenericService<T, ID> {

    T save(T entity);

    List<T> findAll();

    Optional<T> findById(ID id);

    T update(ID id, T entity);

    void deleteById(ID id);
}
