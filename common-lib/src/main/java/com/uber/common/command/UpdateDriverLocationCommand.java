package com.uber.common.command;

import com.uber.common.Coordinates;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDriverLocationCommand {
    private Coordinates newCoordinates;
}