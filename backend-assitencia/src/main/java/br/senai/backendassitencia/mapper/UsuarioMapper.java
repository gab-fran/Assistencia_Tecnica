package br.senai.backendassitencia.mapper;


import br.senai.backendassitencia.dto.usuario.UsuarioCreateDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioResponseDTO;
import br.senai.backendassitencia.dto.usuario.UsuarioUpdateDTO;
import br.senai.backendassitencia.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "statusUsuario", ignore = true)
    Usuario toEntity(UsuarioCreateDTO dto);

    UsuarioResponseDTO toResponse(Usuario usuario);

    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "statusUsuario", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UsuarioUpdateDTO dto, @MappingTarget Usuario usuario);
}
