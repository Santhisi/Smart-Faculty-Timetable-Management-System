package com.smartfaculty.controller;

import com.smartfaculty.entity.TimeSlot;
import com.smartfaculty.repository.TimeSlotRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-slots")

public class TimeSlotController {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    // GET ACTIVE TIME SLOTS
    @GetMapping
    public ResponseEntity<List<TimeSlot>> getAllTimeSlots() {

        return ResponseEntity.ok(
                timeSlotRepository.findByActiveTrue()
        );
    }

    // CREATE TIME SLOT
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TimeSlot> createTimeSlot(
            @RequestBody TimeSlot timeSlot) {

        return ResponseEntity.ok(
                timeSlotRepository.save(timeSlot)
        );
    }

    // UPDATE TIME SLOT
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TimeSlot> updateTimeSlot(
            @PathVariable Long id,
            @RequestBody TimeSlot timeSlot) {

        TimeSlot existingTimeSlot =
                timeSlotRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Time slot not found with id: " + id
                                )
                        );

        existingTimeSlot.setSlotNumber(
                timeSlot.getSlotNumber()
        );

        existingTimeSlot.setDay(
                timeSlot.getDay()
        );

        existingTimeSlot.setStartTime(
                timeSlot.getStartTime()
        );

        existingTimeSlot.setEndTime(
                timeSlot.getEndTime()
        );

        existingTimeSlot.setActive(
                timeSlot.getActive()
        );

        return ResponseEntity.ok(
                timeSlotRepository.save(existingTimeSlot)
        );
    }

    // DELETE / DEACTIVATE TIME SLOT
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTimeSlot(
            @PathVariable Long id) {

        TimeSlot existingTimeSlot =
                timeSlotRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Time slot not found with id: " + id
                                )
                        );

        existingTimeSlot.setActive(false);

        timeSlotRepository.save(existingTimeSlot);

        return ResponseEntity.ok(
                "Time slot deactivated successfully."
        );
    }
}

