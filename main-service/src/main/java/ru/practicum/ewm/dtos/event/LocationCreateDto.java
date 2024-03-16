package ru.practicum.ewm.dtos.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class LocationCreateDto {

    @NotNull
    Float lat;
    @NotNull
    Float lon;
}
