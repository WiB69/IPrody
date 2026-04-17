package com.iprody.crm.inventoryservice.mapper;

import com.iprody.crm.inventoryservice.model.dto.GroupData;
import com.iprody.crm.inventoryservice.model.dto.GroupUpdateData;
import com.iprody.crm.inventoryservice.model.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupMapper {
    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    Group fromData(GroupData data);

    @Mapping(target = "currentCount", source = "data.currentCount")
    Group update(Group group, GroupUpdateData data);
}
