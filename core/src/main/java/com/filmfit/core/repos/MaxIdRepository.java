package com.filmfit.core.repos;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface MaxIdRepository<T> extends Repository<T, Long> {
    @Query(value = "SELECT MAX(id) FROM #{#entityName}", nativeQuery = true)
    long findMaxId();
}
