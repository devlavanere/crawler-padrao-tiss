import javax.mail.Session
import javax.mail.PasswordAuthentication
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.Message
import javax.mail.Transport

class EmailManager {
    private File arquivoEmails

    EmailManager() {
        arquivoEmails = new File("./Downloads/emails_interessados.txt")
        if (!arquivoEmails.exists()) {
            arquivoEmails.createNewFile()
        }
    }

    // CREATE
    void adicionarEmail(String email) {
        def emails = listarEmailsLista()
        if (!emails.contains(email)) {
            arquivoEmails.append(email + "\n")
            println "Email [${email}] adicionado à lista de interessados."
        }
    }

    // READ
    void listarEmails() {
        println "\n--- Lista de Emails Interessados ---"
        if (arquivoEmails.exists() && arquivoEmails.length() > 0) {
            arquivoEmails.eachLine { println "- ${it}" }
        } else {
            println "Nenhum e-mail cadastrado."
        }
        println "------------------------------------\n"
    }

    List<String> listarEmailsLista() {
        return arquivoEmails.exists() ? arquivoEmails.readLines() : []
    }

    // DELETE
    void removerEmail(String email) {
        def emails = listarEmailsLista()
        if (emails.remove(email)) {
            arquivoEmails.text = emails.join("\n") + (emails ? "\n" : "")
            println "Email [${email}] removido da lista."
        }
    }

    // ENVIO DE E-MAIL (FUNCIONALIDADE EXTRA)
    void enviarRelatorio(String assunto, String corpo) {
        // TODO: Substitua pelas suas credenciais geradas (Senha de App)
        String remetente = "COLOQUE_O_EMAIL_AQUI"
        String senha = "COLOQUE_A_SENHA_AQUI"
        String host = "smtp.gmail.com"

        Properties properties = System.getProperties()
        properties.put("mail.smtp.host", host)
        properties.put("mail.smtp.port", "587")
        properties.put("mail.smtp.starttls.enable", "true")
        properties.put("mail.smtp.auth", "true")

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remetente, senha)
            }
        })

        try {
            MimeMessage message = new MimeMessage(session)
            message.setFrom(new InternetAddress(remetente))

            def listaEmails = listarEmailsLista()
            if (listaEmails.isEmpty()) {
                println "Nenhum e-mail na lista para enviar o relatório."
                return
            }

            def destinatarios = listaEmails.join(",")
            message.addRecipients(Message.RecipientType.TO, destinatarios)
            message.setSubject(assunto)
            message.setText(corpo)

            println "Enviando e-mail para: ${destinatarios}..."
            Transport.send(message)
            println "E-mail enviado com sucesso!"
        } catch (Exception mex) {
            println "ERRO AO ENVIAR E-MAIL: Verifique sua senha de App e a conexão."
            mex.printStackTrace()
        }
    }
}