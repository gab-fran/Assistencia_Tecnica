package br.senai.backendassitencia.mapper;

import br.senai.backendassitencia.dto.equipamento.EquipamentoCreateDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoResponseDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoResumoDTO;
import br.senai.backendassitencia.dto.equipamento.EquipamentoUpdateDTO;
import br.senai.backendassitencia.entity.Cliente;
import br.senai.backendassitencia.entity.Equipamento;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipamentoMapper {

    @Mapping(target = "idEquipamento", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "statusEquipamento", ignore = true)
    @Mapping(target = "cliente", source = "idCliente")
    Equipamento toEntity(EquipamentoCreateDTO dto);

    EquipamentoResponseDTO toResponse(Equipamento equipamento);

    List<EquipamentoResponseDTO> toResponseList(List<Equipamento> equipamentos);

    EquipamentoResumoDTO toResumo(Equipamento equipamento);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idEquipamento", ignore = true)
    @Mapping(target = "statusEquipamento", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "cliente", source = "idCliente")
    void updateEntity(EquipamentoUpdateDTO dto, @MappingTarget Equipamento equipamento);

    default Cliente mapCliente(Integer idCliente) {
        if (idCliente == null) {
            return null;
        }
        return Cliente.builder().idCliente(idCliente).build();
    }
}
