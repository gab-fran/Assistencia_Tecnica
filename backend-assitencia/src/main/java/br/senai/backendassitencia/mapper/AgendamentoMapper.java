package br.senai.backendassitencia.mapper;

import br.senai.backendassitencia.dto.agendamento.AgendamentoCreateDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoResponseDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoResumoDTO;
import br.senai.backendassitencia.dto.agendamento.AgendamentoUpdateDTO;
import br.senai.backendassitencia.entity.Agendamento;
import br.senai.backendassitencia.entity.Equipamento;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = EquipamentoMapper.class)
public interface AgendamentoMapper {

	@Mapping(target = "idAgendamento", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "statusAgendamento", ignore = true)
	@Mapping(target = "equipamento", source = "idEquipamento")
	Agendamento toEntity(AgendamentoCreateDTO dto);

	AgendamentoResponseDTO toResponse(Agendamento agendamento);

	AgendamentoResumoDTO toResumo(Agendamento agendamento);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "idAgendamento", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "statusAgendamento", ignore = true)
	@Mapping(target = "equipamento", source = "idEquipamento")
	void updateEntity(AgendamentoUpdateDTO dto, @MappingTarget Agendamento agendamento);

	default Equipamento mapEquipamento(Integer idEquipamento) {
		if (idEquipamento == null) {
			return null;
		}
		return Equipamento.builder().idEquipamento(idEquipamento).build();
	}
}
