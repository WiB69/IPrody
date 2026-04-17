package com.iprody.crm.inventoryservice.model.service;

import com.iprody.crm.inventoryservice.model.dto.GroupData;
import com.iprody.crm.inventoryservice.model.dto.GroupFilter;
import com.iprody.crm.inventoryservice.model.dto.GroupUpdateData;
import com.iprody.crm.inventoryservice.model.entity.Group;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    Group findById(UUID id);

    Group save(GroupData data);

    Group update(UUID id, GroupUpdateData data);

    List<Group> findAllByFilter(GroupFilter filter, Integer offset, Integer limit);
}
