package com.condominio.gestao_condominio.repository;

import com.condominio.gestao_condominio.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    //buscar ocorrencias filtrando o id do usuario morador associado
    List<Ocorrencia> findByMoradorId(Long idMorador);
}