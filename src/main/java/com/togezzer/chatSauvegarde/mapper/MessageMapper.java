package com.togezzer.chatSauvegarde.mapper;

import com.togezzer.chatSauvegarde.dto.MessageDTO;
import com.togezzer.chatSauvegarde.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "uuid", ignore = true)
    MessageEntity toEntity(MessageDTO dto);

    MessageDTO toDto(MessageEntity entity);
}
