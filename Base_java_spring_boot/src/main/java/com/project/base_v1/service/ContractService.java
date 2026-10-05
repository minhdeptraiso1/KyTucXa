package com.project.base_v1.service;

import com.project.base_v1.dto.request.contract.*;
import com.project.base_v1.dto.response.contract.ContractResponse;
import com.project.base_v1.enums.ContractStatus;
import org.springframework.data.domain.*;
import java.util.*;

public interface ContractService {
    ContractResponse create(CreateContractRequest request);
    ContractResponse activate(UUID id);
    ContractResponse renew(UUID id, RenewContractRequest request);
    ContractResponse terminate(UUID id, TerminateContractRequest request);
    List<ContractResponse> getMyContracts();
    Page<ContractResponse> getAll(ContractStatus status, Pageable pageable);
}
