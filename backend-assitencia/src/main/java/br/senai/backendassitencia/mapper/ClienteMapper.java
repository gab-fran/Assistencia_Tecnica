package br.senai.backendassitencia.mapper;

import br.senai.backendassitencia.dto.cliente.ClienteCreateDTO;
import br.senai.backendassitencia.dto.cliente.ClienteResponseDTO;
import br.senai.backendassitencia.dto.cliente.ClienteResumoDTO;
import br.senai.backendassitencia.dto.cliente.ClienteUpdateDTO;
import br.senai.backendassitencia.entity.Cliente;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "statusCliente", ignore = true)
    Cliente toEntity(ClienteCreateDTO dto);

    ClienteResponseDTO toResponse(Cliente cliente);

    ClienteResumoDTO toResumo(Cliente cliente);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "statusCliente", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(ClienteUpdateDTO dto, @MappingTarget Cliente cliente);
}
