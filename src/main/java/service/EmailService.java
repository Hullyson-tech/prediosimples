package com.condominio.gestao_condominio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmailAprovacao(String destinatario, String nomeUsuario, String senha){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject("Acesso Aprovado - Sistema de Gestão de Condomínio");
            message.setText("Olá, "+ nomeUsuario + "!\n\n" +
                    "O seu cadastro no sistema do condomínio foi aprovado com sucesso.\n\n" +
                    "Aqui está estão os seus dados de acesso:\n" +
                    "E-mail (login): " + destinatario + "\n" +
                    "Senha: "+ senha + "\n\n" +
                    "Já pode acessar o App e desfrutar dos recursos disponíveis.\n\n" +
                    "Atenciosamente,\nAdministração do Condomínio");

            mailSender.send(message);
        } catch (Exception e){
            throw new RuntimeException("Erro ao enviar o e-mail de aprovação: " + e.getMessage());
        }
    }
}