package com.airops.handling.service;

import com.airops.handling.dto.request.CreateSickLeaveRequest;
import com.airops.handling.dto.response.SickLeaveResponse;
import com.airops.handling.exception.ResourceNotFoundException;
import com.airops.handling.model.SickLeave;
import com.airops.handling.model.Staff;
import com.airops.handling.model.enums.SickLeaveStatus;
import com.airops.handling.repository.SickLeaveRepository;
import com.airops.handling.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SickLeaveService {

    private final SickLeaveRepository sickLeaveRepo;
    private final StaffRepository staffRepo;

    public List<SickLeaveResponse> findAll() {
        return sickLeaveRepo.findAllByOrderByStartDateDesc().stream().map(SickLeaveResponse::from).toList();
    }

    public List<SickLeaveResponse> findPending() {
        return sickLeaveRepo.findByStatusOrderByStartDateDesc(SickLeaveStatus.CERT_PENDING)
                .stream().map(SickLeaveResponse::from).toList();
    }

    public List<SickLeaveResponse> findByStaff(Long staffId) {
        return sickLeaveRepo.findByStaffIdOrderByStartDateDesc(staffId)
                .stream().map(SickLeaveResponse::from).toList();
    }

    @Transactional
    public SickLeaveResponse create(CreateSickLeaveRequest req) {
        Staff staff = staffRepo.findById(req.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", req.staffId()));

        SickLeave sl = SickLeave.builder()
                .staff(staff)
                .startDate(req.startDate())
                .endDate(req.endDate())
                .type(req.type())
                .note(req.note())
                .status(SickLeaveStatus.CERT_PENDING)
                .build();

        return SickLeaveResponse.from(sickLeaveRepo.save(sl));
    }

    @Transactional
    public SickLeaveResponse updateStatus(Long id, SickLeaveStatus status) {
        SickLeave sl = sickLeaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SickLeave", id));
        sl.setStatus(status);
        return SickLeaveResponse.from(sickLeaveRepo.save(sl));
    }

    @Transactional
    public SickLeaveResponse uploadCertificate(Long id, String certificateUrl) {
        SickLeave sl = sickLeaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SickLeave", id));
        sl.setCertificateUrl(certificateUrl);
        sl.setStatus(SickLeaveStatus.CERT_RECEIVED);
        return SickLeaveResponse.from(sickLeaveRepo.save(sl));
    }
}
