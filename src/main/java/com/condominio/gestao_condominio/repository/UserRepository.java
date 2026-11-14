package com.condominio.gestao_condominio.repository;

import com.condominio.gestao_condominio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    List<User> findByStatusAprovacao(String statusAprovacao);

    Optional<User> findByEmailOrPhone(String email, String phone);
}