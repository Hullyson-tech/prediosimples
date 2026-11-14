package com.condominio.gestao_condominio.controller;

import com.condominio.gestao_condominio.model.Ocorrencia;
import com.condominio.gestao_condominio.model.User;
import com.condominio.gestao_condominio.repository.OcorrenciaRepository;
import com.condominio.gestao_condominio.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    @Autowired
    private OcorrenciaRepository ocorrenciaRepository;

    @Autowired
    private UserRepository userRepository; //validar se o morador está APROVADO

    //listar todas as ocorrencias (mural visivel para moradores acompanhar as ocorrencias comuns)
    @GetMapping
    public List<Ocorrencia> listarTodas() {
        return ocorrenciaRepository.findAll();
    }

    // listar ocorrencias de um morador específico (apenas roles SINDICO E SUPORTE podem ter acesso)
    @GetMapping("/morador/{idMorador}")
    public List<Ocorrencia> listarPorMorador(
            @PathVariable Long idMorador,
            @RequestParam String roleQuemExecuta){

        if (!"SINDICO".equalsIgnoreCase(roleQuemExecuta) && !"SUPORTE".equalsIgnoreCase(roleQuemExecuta)){
            throw new RuntimeException("Acesso negado: apenas o Síndico pode consultar o histórico de um morador.");
        }

        return ocorrenciaRepository.findByMoradorId(idMorador);
    }

    //abrir uma nova ocorrencia (apenas morador aprovado)
    @PostMapping
    public Ocorrencia criarOcorrencia(
            @RequestParam Long idMorador, //recebe o ID para validar o status
            @Valid @RequestBody Ocorrencia ocorrencia) {

        //busca usuario na no banco
        User morador = userRepository.findById(idMorador)
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado."));

        if (!"MORADOR".equalsIgnoreCase(morador.getRole())){
            throw new RuntimeException("Apenas moradores podem abrir novas ocorrências.");
        }

        //validacao do cadastro aprovado pelo sindico
        if (!"APROVADO".equalsIgnoreCase(morador.getStatusAprovacao())) {
            throw new RuntimeException("Acesso pendente ou rejeitado:entre em contato com sindico ou suporte.");
        }

        // associar o objeto User (morador) à ocorrência
        ocorrencia.setMorador(morador);
        ocorrencia.setStatus("ABERTO");
        return ocorrenciaRepository.save(ocorrencia);
    }

    //atualizar o status da ocorrencia(apenas as roles sindico e suporte)
    @PostMapping("/{id}/status")
    public Ocorrencia alterarStatusOcorrencia(
            @PathVariable Long id,
            @RequestParam String roleQuemExecuta,
            @RequestParam String novoStatus) { // ex: em andamento ou concluido

        if (!"SINDICO".equalsIgnoreCase(roleQuemExecuta) && !"SUPORTE".equalsIgnoreCase(roleQuemExecuta)){
            throw new RuntimeException("Permissão negada: apenas o Síndico pode alterar o status de uma ocorrência.");
        }
        Ocorrencia ocorrencia = ocorrenciaRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Ocorrência não encontrada."));

        String statusMaiusculo = novoStatus.toUpperCase();
        if (!statusMaiusculo.equals("ABERTO") && !statusMaiusculo.equals("EM_ANDAMENTO")&& !statusMaiusculo.equals("CONCLUIDO")){
            throw new RuntimeException("Status inválido. Utilize: ABERTO, EM_ANDAMENTO ou CONCLUIDO.");
        }

        ocorrencia.setStatus(statusMaiusculo);
        return ocorrenciaRepository.save(ocorrencia);
        }
}


