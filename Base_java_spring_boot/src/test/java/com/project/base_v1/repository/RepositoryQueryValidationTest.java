package com.project.base_v1.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryQueryValidationTest {
    @Autowired BedRepository bedRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired RoomAssignmentRepository assignmentRepository;
    @Autowired ContractRepository contractRepository;

    @Test
    void phaseThreeAndFourRepositoriesStartWithValidJpaQueries() {
        assertNotNull(bedRepository);
        assertNotNull(registrationRepository);
        assertNotNull(assignmentRepository);
        assertNotNull(contractRepository);
    }
}
