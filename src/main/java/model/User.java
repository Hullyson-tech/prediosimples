package com.condominio.gestao_condominio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity  // isso é uma tabela do banco
@Table(name = "tb_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, max = 20, message = "A senha deve ter pelo menos de 6 a 20 digitos")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "A senha deve conter apenas letras e números.")
    @Column(nullable = false)
    private String password;
    
    private String phone;

    @NotBlank(message = "O perfil é obrigatório")
    @Column(nullable = false)
    private String role; //ex: morador, sindico ou suporte

    private  String apartmentNumber;

    //controle de aprovação do sindico/suporte
    @Column(name = "status_aprovacao", nullable = false)
    private String statusAprovacao = "PENDENTE"; //PENDENTE, APROVADO, REJEITADO

    // padrão numérico 1 para ativo e 0 para inativo
    @Column(nullable = false)
    private Integer ativo = 1;

    //Relacionamento com as ocorrências abertas pelos moradores
    @OneToMany(mappedBy = "morador", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Ocorrencia> ocorrencias;

    //data e hora de criação
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //data e hora de atualização
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}





