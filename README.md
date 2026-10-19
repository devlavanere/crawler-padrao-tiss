# Bot Web Crawler - Padrão TISS (ANS)

**Desenvolvido por:** Michel Lavanere Sampaio  
**Desafio:** Acelera ZG

## Sobre o Projeto
Este projeto consiste em um Web Crawler/Scraper desenvolvido em **Groovy** para automatizar a coleta de dados e arquivos de acesso livre no portal da Agência Nacional de Saúde Suplementar (ANS), especificamente na seção do Padrão TISS (Troca de Informação de Saúde Suplementar).

A automação acessa as páginas do governo, realiza o parser do HTML utilizando **HTTPBuilder** e **Jsoup**, navega pelas tabelas dinamicamente com suporte a **Regex** e salva os dados estruturados localmente.

## Funcionalidades Implementadas

O robô cumpre rigorosamente as 3 Tasks obrigatórias e os requisitos opcionais:

- **[Task 1] Download do Componente de Comunicação:** Acessa a página base via HTTPBuilder, encontra dinamicamente a versão mais recente do Padrão TISS na página e faz o download automático do arquivo `.zip` correspondente.
- **[Task 2] Extração de Histórico (CSV):** Acessa o histórico de versões, faz o parser da tabela no HTML e extrai os dados (Competência, Publicação e Início de Vigência), filtrando via Regex apenas os registros a partir de **Janeiro de 2016**, salvando-os no arquivo `historico_tiss.csv`.
- **[Task 3] Download da Tabela de Erros:** Acessa a página de Tabelas Relacionadas e realiza o download da "Tabela de erros no envio para a ANS" em formato `.xlsx`.
- **[Requisito Opcional] Gestão de Interessados (CRUD) e Notificação (E-mail):** Implementa um CRUD em arquivo de texto (`emails_interessados.txt`) para gerenciar destinatários. Ao final da extração, dispara um relatório via servidor SMTP (`javax.mail`) notificando que os arquivos foram baixados com sucesso.

## Tecnologias Utilizadas

- **Linguagem:** Groovy 3.0.22
- **Gerenciador de Dependências:** Gradle
- **Requisições HTTP:** HTTPBuilder (0.7.1)
- **Parser de HTML/DOM:** Jsoup (1.15.3)
- **Envio de E-mails:** JavaMail API (1.6.2)
- **Recursos Nativos:** Expressões Regulares (`java.util.regex`), Manipulação de Arquivos e NIO (`java.nio.file`).
