package com.finanscepte.common;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Tüm MongoDB tabanlı servislerde ortak tip güvenli veri erişimi.
 *
 * @param <T>  belge (entity) tipi
 * @param <ID> birincil anahtar tipi
 */
@NoRepositoryBean
public interface GenericRepository<T, ID> extends MongoRepository<T, ID> {
}
