package com.togezzer.chat_sauvegarde.mapper;

import com.togezzer.chat_sauvegarde.dto.MessageDTO;
import com.togezzer.chat_sauvegarde.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "uuid", ignore = true)
    MessageEntity toEntity(MessageDTO dto);

    MessageDTO toDto(MessageEntity entity);
}
