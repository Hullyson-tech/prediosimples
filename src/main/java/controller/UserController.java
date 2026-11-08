package com.condominio.gestao_condominio.controller;

import com.condominio.gestao_condominio.model.User;
import com.condominio.gestao_condominio.model.UserLog;
import com.condominio.gestao_condominio.repository.UserLogRepository;
import com.condominio.gestao_condominio.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLogRepository userLogRepository; // irá gerar logs

    @Autowired
    private EmailService emailService; // serviço de e-mail

    @Autowired
    private SmsService smsService; //serviço de sms (em stand-by via application)

    //instancia do codificador BCrypt para blindar as senhas
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //listar usuários cadastrados e ativos
    @GetMapping
    public List<User> listarAtivos() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getAtivo() != null && user.getAtivo() == 1)
                .toList();
    }


    //listar todos os logs
    @GetMapping("/logs")
    public List<UserLog> listarLogsAuditoria() {
        return userLogRepository.findAll();
    }

    // Criar novos usuarios com senha criptografada via BCrypt e registrar logs
    @PostMapping
    public User criarUsuario(@Valid @RequestBody User user) {
        user.setAtivo(1); //para garantir que sempre o novo cadastro já grave como ativo na tabela
        user.setStatusAprovacao("PENDENTE");

        //criptografar a senha antes de salvar no banco de dados (segurança OWASP)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User salvo = userRepository.save(user);

        //Registrar os logs
        UserLog log = new UserLog();
        log.setUserId(salvo.getId());
        log.setAction("CRIACAO");
        log.setDescription("Novo usuário cadastrado com sucesso, aguardando aprovação do síndico.");
        userLogRepository.save(log);

        return salvo;
    }

    //Listar usuários com cadastro Pendente(apenas roles sindico e suporte)
    @GetMapping("/pendentes")
    public List<User> listarPendentes(@RequestParam String roleQuemExecuta) {
        if (!"SINDICO".equalsIgnoreCase(roleQuemExecuta) && !"SUPORTE".equalsIgnoreCase(roleQuemExecuta)) {
            throw new RuntimeException("Acesso negado: apenas Síndico pode visualizar a lista de cadastros.");
        }
        return userRepository.findByStatusAprovacao("PENDENTE");
    }

    //Aprovar ou rejeitar acesso do usuário(apenas role sindico o suporte)
    @GetMapping("/{id}/aprovar")
    public User alterarStatusAprovacao(
            @PathVariable Long id,
            @RequestParam String roleQuemExecuta,
            @RequestParam String novoStatusAprovacao) {

        if (!"SINDICO".equalsIgnoreCase(roleQuemExecuta) && !"SUPORTE".equalsIgnoreCase(roleQuemExecuta)) {
            throw new RuntimeException("Acesso negado: apenas Sindico pode aprovar cadastros.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String statusMaiusculo = novoStatusAprovacao.toUpperCase();
        if (!statusMaiusculo.equals("APROVADO") && !statusMaiusculo.equals("REJEITADO")) {
            throw new RuntimeException("Status inválido. Utilize APROVADO ou REJEITADO.");

        }

        user.setStatusAprovacao(statusMaiusculo);
        User userAtualizado = userRepository.save(user);

        //se o status for APROVADO, irá disparar a notificação de email ou sms
        if ("APROVADO".equals(statusMaiusculo)) {
            emailService.enviarEmailAprovacao(userAtualizado.getEmail(), userAtualizado.getName(), "[SENHA_CADASTRADA]");
            smsService.enviarSmsAprovacao(userAtualizado.getPhone(), userAtualizado.getName());
        }

        //registrar log de aprovação/rejeição
        UserLog log = new UserLog();
        log.setUserId(userAtualizado.getId());
        log.setAction("APROVACAO_STATUS");
        log.setDescription("Status de aprovação alterado para: " + statusMaiusculo);
        userLogRepository.save(log);

        return userAtualizado;


    }


    // Buscar usuário por e-mail ou telefone
    @GetMapping("/buscar")
    public User buscarPorEmailOuTelefone(@RequestParam String termo) {
        return userRepository.findByEmailOrPhone(termo, termo)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o e-mail ou telefone: " + termo));

    }

    //Endpoint de login para validar credenciais
    @PostMapping("/login")
    public <ResponseEntity> login(@RequestBody Map<String, String> credenciais) {
        String login = credenciais.get("login");
        String senha = credenciais.get("password");

        if (login == null || senha == null) {
            throw new RuntimeException("Informe o e-mail/telefone e a senha para realizar o login.");
        }

        User user = userRepository.findByEmailOrPhone(login, login)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas. Usuário não encontrado."));

        if (user.getAtivo() == null || user.getAtivo() == 0){
            throw new RuntimeException("Este usuário encontra-se desativado.");
        }

        // valida se acesso foi aprovado
        if(!"APROVADO".equalsIgnoreCase(user.getStatusAprovacao())) {
            throw new RuntimeException("Acesso pendente, entre em contato com o Sindico ou Suporte.");
        }
        // Validação da senha utilizando BCrypt.matches()
        if (!passwordEncoder.matches(senha, user.getPassword())){
            throw new RuntimeException("Credenciais inválidas. Senha incorreta.");
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mensagem", "Login efetuado com sucesso!");
        resposta.put("nome", user.getName());
        resposta.put("perfil", user.getRole());
        resposta.put("email", user.getEmail());

        return ResponseEntity.ok(resposta);
    }

    // Endpoint PUT para atualização de dados cadastrais
    @PutMapping("/atualizar")
    public User atualizarUsuarioPorContato(
            @RequestParam String contato, // pode utilizar email ou telefone
            @RequestParam String roleQuemExecuta, // perfil de quem está executando a ação
            @RequestBody User dadosAtualizados) {

        //Procura o usuário pelo e-mail e telefone
        User user = userRepository.findByEmailOrPhone(contato, contato)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com os dados informado: " + contato));

        String descricaoLog;

        //valida role de permissão e de permissão de atualização de dados
        if ("MORADOR".equalsIgnoreCase(roleQuemExecuta)) {
            // o morador só pode atualizar e-mail e telefone
            user.setEmail(dadosAtualizados.getEmail());
            user.setPhone(dadosAtualizados.getPhone());
            descricaoLog = "O morador atualizou seus dados de e-mail e telefone.";

        } else if ("SUPORTE".equalsIgnoreCase(roleQuemExecuta) || "SINDICO".equalsIgnoreCase(roleQuemExecuta)){
            //role suporte tem permissão full
            user.setName(dadosAtualizados.getName());
            user.setEmail(dadosAtualizados.getEmail());
            user.setPhone(dadosAtualizados.getPhone());
            user.setApartmentNumber(dadosAtualizados.getApartmentNumber());

            if (dadosAtualizados.getRole() != null) {
                user.setRole(dadosAtualizados.getRole());
            }

            descricaoLog = "O administrador realizou alterações de dados no cadastro.";

        } else {
            throw new RuntimeException("Permissão negada: esse usuário não tem permissão para esse tipo de alteração.");
        }

        User atualizado = userRepository.save(user);

        //Toda e qualquer alteração é registrada na tabela de logs
        UserLog log = new UserLog();
        log.setUserId(atualizado.getId());
        log.setAction("ATUALIZAÇÃO");
        log.setDescription(descricaoLog);
        userLogRepository.save(log);

        return atualizado;
    }

    //soft delete (muda o status para 0) com registro no log
    @PutMapping("/{id}/desativar")
    public User desativarUsuario(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o código" + id));

        user.setAtivo(0); //altera o status para inativo e mantem os dados salvos na tabela
        User desativado = userRepository.save(user);

        //registro de log de desativação
        UserLog log = new UserLog();
        log.setUserId(desativado.getId());
        log.setAction("DESATIVADO");
        log.setDescription("Usuário desativado.");
        userLogRepository.save(log);

        return desativado;
    }
}
