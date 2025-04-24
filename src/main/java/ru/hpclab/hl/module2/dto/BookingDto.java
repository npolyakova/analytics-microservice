package ru.hpclab.hl.module2.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BookingDto {
    long id;
    long guestId;
    long roomId;
    Date dateArr;
    Date dateLeave;
    double cost;
}
