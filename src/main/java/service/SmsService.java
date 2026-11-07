package com.condominio.gestao_condominio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    //Lê a propriedade do application.properties. Se não existir, assume 'false' por segurança.
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    public void enviarSmsAprovacao(String telefoneDestino, String nomeUsuario){

        //regra de stand-by: se a flag for false, sai imeditamente sem disparar sms.
        if(!smsEnabled){
            System.out.println("[SMS em Stand-by] Envio ignorado para o número:" + telefoneDestino + "(Serviço desativado por configuração)");
            return;
        }

        try {
            String mensagem = "Olá, " + nomeUsuario + "! Acesso ao App aprovado. " +
                    "Realize seu login com suas credenciais de login e senha informado no ato do cadastro.";

            // ----------------------------------------------------
            // ESPAÇO DE INTEGRAÇÃO COM O GATEWAY DE SMS
            // Exemplo com Twilio, Zenvia ou Infobip usando RestTemplate:
            // RestTemplate restTemplate = new RestTemplate();
            // restTemplate.postForObject("https://api.sua-operadora.com/v1/send", payload, String.class);
            // ----------------------------------------------------

        } catch (Exception e){
            System.out.println("Sms enviado com sucesso para: " + telefoneDestino);
                throw new RuntimeException("Erro ao enviar o SMS: " + e.getMessage());
        }
    }
}
