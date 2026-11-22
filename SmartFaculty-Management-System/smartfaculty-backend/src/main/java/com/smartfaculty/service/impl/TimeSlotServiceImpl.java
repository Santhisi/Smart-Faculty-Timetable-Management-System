package com.smartfaculty.service.impl;

import com.smartfaculty.entity.TimeSlot;
import com.smartfaculty.repository.TimeSlotRepository;
import com.smartfaculty.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    @Autowired
    private TimeSlotRepository repository;

    @Override
    public TimeSlot saveTimeSlot(TimeSlot timeSlot) {
        return repository.save(timeSlot);
    }

    @Override
    public List<TimeSlot> getAllTimeSlots() {
        return repository.findAll();
    }

    @Override
    public TimeSlot getTimeSlotById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void deleteTimeSlot(Long id) {
        repository.deleteById(id);
    }
}