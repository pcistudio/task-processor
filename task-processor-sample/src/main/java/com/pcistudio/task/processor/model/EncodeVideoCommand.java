package com.pcistudio.task.processor.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class EncodeVideoCommand {
    private UUID videoId;
    private String videoPath;


}
