package com.condominio.gestao_condominio.repository;

import com.condominio.gestao_condominio.model.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {
    //Repositorio pronto para salvar e consulta logs de auditoria de usuarios
}
