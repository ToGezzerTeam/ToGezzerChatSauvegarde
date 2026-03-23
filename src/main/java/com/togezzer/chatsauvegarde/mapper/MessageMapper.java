package com.togezzer.chatsauvegarde.mapper;

import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.entity.MessageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageEntity toEntity(MessageDTO dto);

    MessageDTO toDto(MessageEntity entity);
}
