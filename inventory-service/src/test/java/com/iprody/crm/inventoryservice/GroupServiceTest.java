package com.iprody.crm.inventoryservice;

import com.iprody.crm.inventoryservice.model.dto.GroupData;
import com.iprody.crm.inventoryservice.model.dto.GroupFilter;
import com.iprody.crm.inventoryservice.model.dto.GroupUpdateData;
import com.iprody.crm.inventoryservice.model.entity.Group;
import com.iprody.crm.inventoryservice.model.service.GroupServiceImpl;
import com.iprody.crm.inventoryservice.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Group group = new Group();
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));

        Group result = groupService.findById(id);

        assertNotNull(result);
        verify(groupRepository).findById(id);
    }

    @Test
    void shouldSave() {
        GroupData data = new GroupData();
        Group group = new Group();
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group result = groupService.save(data);

        assertNotNull(result);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        GroupUpdateData updateData = new GroupUpdateData();
        Group existingGroup = new Group();

        when(groupRepository.findById(id)).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArguments()[0]);

        Group result = groupService.update(id, updateData);

        assertNotNull(result);
        verify(groupRepository).findById(id);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void shouldFindAllByFilter() {
        UUID refId = UUID.randomUUID();
        GroupFilter filter = new GroupFilter();
        filter.setGroupRefId(refId);
        filter.setIsAvailFreePlaces(true);

        Group group = new Group();
        Page<Group> mockPage = new PageImpl<>(List.of(group), PageRequest.of(0, 10), 1);

        when(groupRepository.findAllByFilter(eq(refId), eq(true), any()))
                .thenReturn(mockPage);

        List<Group> result = groupService.findAllByFilter(filter, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(groupRepository).findAllByFilter(eq(refId), eq(true), any());
    }

    @Test
    void shouldFindAllByFilterWithEmptyResponse() {
        GroupFilter filter = new GroupFilter();

        when(groupRepository.findAllByFilter(any(), any(), any()))
                .thenReturn(Page.empty());

        List<Group> result = groupService.findAllByFilter(filter, 0, 1);

        assertTrue(result.isEmpty());
        verify(groupRepository, times(1)).findAllByFilter(any(), any(), any());
    }
}
