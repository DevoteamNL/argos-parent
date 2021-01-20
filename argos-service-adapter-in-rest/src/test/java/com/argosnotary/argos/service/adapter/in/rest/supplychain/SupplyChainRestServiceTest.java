/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.service.adapter.in.rest.supplychain;

import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.supplychain.SupplyChain;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSupplyChain;
import com.argosnotary.argos.service.domain.DeleteService;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRestServiceTest {

    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SUPPLY_CHAIN_NAME = "supplyChainName";
    private static final String LABEL_NAME = "labelName";
    @Mock
    private SupplyChainRepository supplyChainRepository;
    @Mock
    private SupplyChainMapper converter;
    @Mock
    private SupplyChain supplyChain;
    @Mock
    private RestSupplyChain restSupplyChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private LabelRepository labelRepository;

    private SupplyChainRestService supplyChainRestService;

    @Mock
    private TreeNode treeNode;

    @Mock
    private DeleteService deleteService;

    @BeforeEach
    public void setup() {
        supplyChainRestService = new SupplyChainRestService(supplyChainRepository, hierarchyRepository, converter, labelRepository, deleteService);
    }

    @Test
    void createSupplyChain_With_UniqueName_Should_Return_201() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(converter.convertFromRestSupplyChainCommand(any())).thenReturn(supplyChain);
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(restSupplyChain);
        ResponseEntity<RestSupplyChain> supplyChainItemResponse = supplyChainRestService.createSupplyChain(restSupplyChain);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertThat(supplyChainItemResponse.getHeaders().getLocation(), notNullValue());
        assertThat(supplyChainItemResponse.getBody(), is(restSupplyChain));
        verify(supplyChainRepository).save(supplyChain);
    }

    @Test
    void createSupplyChain_With_Not_Existing_Parent_Label_Should_Return_400() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.createSupplyChain(restSupplyChain));
        assertThat(exception.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label not found : parentLabelId\""));
    }


    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_200() {
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(of(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(restSupplyChain);
        ResponseEntity<RestSupplyChain> supplyChainItemResponse = supplyChainRestService.getSupplyChain(SUPPLY_CHAIN_ID);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(supplyChainItemResponse.getBody(), is(restSupplyChain));
    }

    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_404() {
        when(supplyChainRepository.findBySupplyChainId(any())).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () ->
                supplyChainRestService.getSupplyChain("supplyChainName"));
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateSupplyChain() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(converter.convertFromRestSupplyChainCommand(restSupplyChain)).thenReturn(supplyChain);
        when(converter.convertToRestRestSupplyChainItem(supplyChain)).thenReturn(restSupplyChain);
        when(supplyChainRepository.update(SUPPLY_CHAIN_ID, supplyChain)).thenReturn(Optional.of(supplyChain));
        ResponseEntity<RestSupplyChain> response = supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restSupplyChain));
        verify(supplyChain).setSupplyChainId(SUPPLY_CHAIN_ID);
    }

    @Test
    void updateSupplyChainNotExits() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(converter.convertFromRestSupplyChainCommand(restSupplyChain)).thenReturn(supplyChain);
        when(supplyChainRepository.update(SUPPLY_CHAIN_ID, supplyChain)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainId\""));
    }

    @Test
    void updateParentLabelNotExits() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label not found : parentLabelId\""));
    }

    @Test
    void getSupplyChainByPathToRoot() {
        when(hierarchyRepository.findByNamePathToRootAndType(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.of(treeNode));
        when(treeNode.getReferenceId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(supplyChain)).thenReturn(restSupplyChain);
        ResponseEntity<RestSupplyChain> response = supplyChainRestService.getSupplyChainByPath(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME));
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restSupplyChain));
    }

    @Test
    void getSupplyChainByPathToRootNotFound() {
        when(hierarchyRepository.findByNamePathToRootAndType(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.empty());
        List<String> labels = List.of(LABEL_NAME);
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.getSupplyChainByPath(SUPPLY_CHAIN_NAME, labels));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainName with path labelName\""));
    }

    @Test
    void getSupplyChainByPathToRootSupplyChainNotFound() {
        when(hierarchyRepository.findByNamePathToRootAndType(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.of(treeNode));
        when(treeNode.getReferenceId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        List<String> labels = List.of(LABEL_NAME);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.getSupplyChainByPath(SUPPLY_CHAIN_NAME, labels));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainName with path labelName\""));
    }

    @Test
    void deleteSupplyChainById() {
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        assertThat(supplyChainRestService.deleteSupplyChainById(SUPPLY_CHAIN_ID).getStatusCodeValue(), is(204));
        verify(deleteService).deleteSupplyChain(SUPPLY_CHAIN_ID);
    }

    @Test
    void deleteSupplyChainByIdNotFound() {
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.deleteSupplyChainById(SUPPLY_CHAIN_ID));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainId\""));

    }
}
