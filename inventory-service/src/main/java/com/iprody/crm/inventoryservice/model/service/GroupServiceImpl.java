package com.iprody.crm.inventoryservice.model.service;

import com.iprody.crm.inventoryservice.mapper.GroupMapper;
import com.iprody.crm.inventoryservice.model.dto.GroupData;
import com.iprody.crm.inventoryservice.model.dto.GroupFilter;
import com.iprody.crm.inventoryservice.model.dto.GroupUpdateData;
import com.iprody.crm.inventoryservice.model.entity.Group;
import com.iprody.crm.inventoryservice.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroupServiceImpl implements GroupService{

    private final GroupRepository groupRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public Group findById(UUID id) {
        return groupRepository.findById(id).orElse(null);
    }

    @Transactional
    public Group save(GroupData data) {
        return groupRepository.save(GroupMapper.INSTANCE.fromData(data));
    }

    @Transactional
    public Group update(UUID id, GroupUpdateData data) {
        return groupRepository.save(
                GroupMapper.INSTANCE.update(
                        findById(id),
                        data
                )
        );
    }

    @Transactional(readOnly = true)
    public List<Group> findAllByFilter(GroupFilter filter, Integer offset, Integer limit) {
        return groupRepository.findAllByFilter(
                filter.getGroupRefId(),
                filter.getIsAvailFreePlaces(),
                PageRequest.of(offset, limit)).getContent();
    }
}
