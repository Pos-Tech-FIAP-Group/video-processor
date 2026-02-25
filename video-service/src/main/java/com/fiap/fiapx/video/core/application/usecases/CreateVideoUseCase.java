package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand;
import com.fiap.fiapx.video.core.domain.model.Video;

public interface CreateVideoUseCase {
        Video execute(CreateVideoCommand command);
}