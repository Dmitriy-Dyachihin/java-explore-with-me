package ru.practicum.ewm.dtos.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class LocationUpdateDto {

    Float lat;
    Float lon;
}
