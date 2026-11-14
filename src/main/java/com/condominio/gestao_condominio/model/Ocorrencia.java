package com.condominio.gestao_condominio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.condominio.gestao_condominio.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ocorrencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O titulo da ocorrencia é obrigatório")
    @Column(nullable = false)
    private String titulo;

    //campo para detalhar problemas(ex:lampada queimada)
    @NotBlank(message = "A descrição da ocorrência é obrigatória.")
    @Column(nullable = false, length = 1000)
    private String descricao;

    //status: aberto, em andamento, concluido
    @Column(nullable = false)
    private String status = "ABERTO"; // ABERTO, EM_ANDAMENTO, CONCLUIDO

    // Relacionamento ManyToOne: Muitas ocorrencias pertencem a um morador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User morador;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}