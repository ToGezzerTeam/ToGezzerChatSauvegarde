package com.togezzer.chatsauvegarde.mapper;

import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "uuid", ignore = true)
    MessageEntity toEntity(MessageDTO dto);

    MessageDTO toDto(MessageEntity entity);
}
