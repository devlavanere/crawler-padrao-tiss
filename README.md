# Bot Web Crawler - Padrão TISS (ANS)

**Desenvolvido por:** Michel Lavanere Sampaio

## Descrição do Projeto
Este projeto é um Web Crawler/Scraper desenvolvido em Groovy. O objetivo é acessar o site do Governo (Agência Nacional de Saúde Suplementar - ANS), navegar pelas páginas referentes ao Padrão TISS e extrair automaticamente documentações, históricos de versão e planilhas relacionadas.

## Funcionalidades (Tasks)
1. **Download do Padrão TISS:** Acessa a versão mais recente do padrão TISS e faz o download automático do arquivo do "Componente de Comunicação".
2. **Scraping do Histórico:** Navega pela tabela de histórico de versões e extrai os dados de competência, publicação e início de vigência (a partir de Janeiro/2016), salvando-os em um arquivo `.csv`.
3. **Download da Tabela de Erros:** Acessa as tabelas relacionadas e baixa a planilha atualizada de erros no envio para a ANS.
4. **(Opcional) Gestão de E-mails:** Sistema CRUD básico utilizando manipulação de arquivos de texto para gerenciar uma lista de e-mails de interessados, com integração SMTP preparada para disparar os arquivos/relatórios via e-mail.

## Tecnologias Utilizadas
- **Linguagem:** Groovy
- **Bibliotecas:** Jsoup (para parser do HTML) e `javax.mail` (para envio de emails).
- **Recursos Nativos:** IO/NIO para manipulação de arquivos locais e Regex (`java.util.regex`) para extração inteligente de datas e links nas tabelas.

## Como Executar
1. Certifique-se de ter o Groovy instalado na sua máquina (recomendável gerenciá-lo pelo SDKMAN!).
2. Clone este repositório.
3. No terminal, navegue até a raiz do projeto e execute:
   ```bash
   groovy TissCrawler.groovy