package com.togezzer.chatsauvegarde.mapper;

import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageEntity toEntity(MessageDTO dto);

    MessageDTO toDto(MessageEntity entity);

    void updateEntityFromDto(MessageDTO messageDTO, @MappingTarget MessageEntity messageEntity);
}
