package com.project.base_v1.service;

import com.project.base_v1.dto.request.facility.BatchCreateBedsRequest;
import com.project.base_v1.dto.request.facility.CreateBedRequest;
import com.project.base_v1.dto.request.facility.UpdateBedRequest;
import com.project.base_v1.dto.response.facility.BedResponse;
import com.project.base_v1.enums.BedStatus;

import java.util.List;
import java.util.UUID;

public interface BedService {

    BedResponse createBed(CreateBedRequest request);

    List<BedResponse> batchCreateBeds(BatchCreateBedsRequest request);

    BedResponse updateBed(UUID id, UpdateBedRequest request);

    BedResponse updateBedStatus(UUID id, BedStatus status);

    BedResponse getBedById(UUID id);

    List<BedResponse> getBedsByRoom(UUID roomId);

    void deleteBed(UUID id);
}
