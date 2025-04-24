package ru.hpclab.hl.module2.dto;

import lombok.Data;

@Data
public class HotelRoomDto {
    long id;
    RoomType type;
    long costPerNight;
}
