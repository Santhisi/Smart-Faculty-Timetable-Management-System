package com.smartfaculty.service;

import com.smartfaculty.entity.TimeSlot;

import java.util.List;

public interface TimeSlotService {

    TimeSlot saveTimeSlot(TimeSlot timeSlot);

    List<TimeSlot> getAllTimeSlots();

    TimeSlot getTimeSlotById(Long id);

    void deleteTimeSlot(Long id);

}