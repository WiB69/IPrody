package com.iprody.crm.inventoryservice.repository;


import com.iprody.crm.inventoryservice.model.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    @Query("""
            select g from Group g
            where(:groupRefId is null or g.groupRefId = :groupRefId)
            and (
                 :isAvailFreePlaces is null
                 or (:isAvailFreePlaces = true and g.currentCount < g.limit)
                 or (:isAvailFreePlaces = false and g.currentCount >= g.limit)
             )
            """)
    Page<Group> findAllByFilter(
            @Param("groupRefId") UUID groupRefId,
            @Param("isAvailFreePlaces") Boolean isAvailFreePlaces,
            Pageable pageable
    );
}
