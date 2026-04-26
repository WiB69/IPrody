package com.iprody.crm.customerservice;

import com.iprody.crm.customerservice.config.SpringBootApplicationTest;
import com.iprody.crm.customerservice.entity.Contract;
import com.iprody.crm.customerservice.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class ContractRepositoryTest extends SpringBootApplicationTest {

    @Autowired
    private ContractRepository contractRepository;

    @BeforeEach
    void setUp() {
        contractRepository.deleteAll();

        Contract contract1 = new Contract("user1@example.com", "+1234567890");
        Contract contract2 = new Contract("user2@example.com", "+0987654321");
        Contract contract3 = new Contract("user3@example.com", "+1122334455");

        contractRepository.saveAll(List.of(contract1, contract2, contract3));
    }

    @Test
    void shouldSaveContract() {
        Contract contract = new Contract("new@example.com", "+9999999999");

        Contract saved = contractRepository.save(contract);

        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getEmail()).isEqualTo("new@example.com"),
                () -> assertThat(saved.getPhoneNumber()).isEqualTo("+9999999999")
        );
    }

    @Test
    void shouldFindContractById() {
        Contract contract = contractRepository.findAll().get(0);

        Contract found = contractRepository.findById(contract.getId()).orElse(null);

        assertAll(
                () -> assertThat(found).isNotNull(),
                () -> assertThat(found.getId()).isEqualTo(contract.getId()),
                () -> assertThat(found.getEmail()).isEqualTo(contract.getEmail())
        );
    }

    @Test
    void shouldFindAllContracts() {
        List<Contract> contracts = contractRepository.findAll();

        assertThat(contracts).hasSize(3);
    }

    @Test
    void shouldUpdateContract() {
        Contract contract = contractRepository.findAll().get(0);
        contract.setEmail("updated@example.com");

        Contract updated = contractRepository.save(contract);

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void shouldDeleteContract() {
        Contract contract = contractRepository.findAll().get(0);
        UUID contractId = contract.getId();

        contractRepository.delete(contract);

        assertThat(contractRepository.findById(contractId)).isEmpty();
    }
}
