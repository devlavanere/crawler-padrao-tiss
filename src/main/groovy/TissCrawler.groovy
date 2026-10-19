import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class TissCrawler {

    static final String BASE_URL = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss"
    static final String DOWNLOAD_DIR = "./Downloads/Arquivos_padrao_TISS"

    static void main(String[] args) {
        println "Iniciando o Crawler do Padrão TISS..."
        setupDirectory()

        task1ObterComponenteComunicacao()
        task2ObterHistoricoVersoes()
        task3BaixarTabelaErros()

        println "\n--- Executando Requisitos Opcionais (CRUD e Email) ---"
        EmailManager emailManager = new EmailManager()

        emailManager.adicionarEmail("COLOQUE_O_EMAIL_AQUI")
        emailManager.listarEmails()

        emailManager.enviarRelatorio(
                "Crawler TISS Finalizado",
                "Olá! O robô finalizou a execução. Os arquivos do Componente de Comunicação, " +
                        "a tabela de Erros da ANS e o Histórico em CSV foram salvos com sucesso na pasta Downloads do projeto."
        )

        println "\nProcesso concluído com sucesso! Verifique a pasta: ${DOWNLOAD_DIR}"
    }

    static void setupDirectory() {
        def dir = Paths.get(DOWNLOAD_DIR)
        if (!Files.exists(dir)) {
            Files.createDirectories(dir)
        }
    }

    static Document acessarPaginaComHttpBuilder(String urlString) {
        def http = new HTTPBuilder(urlString)
        Document documentoHtml = null

        // Faz a requisição GET solicitando texto puro para passarmos ao Jsoup
        http.request(GET, TEXT) { req ->
            headers.'User-Agent' = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' // Finge ser um navegador

            response.success = { resp, reader ->
                // Uso de Jsoup APENAS para fazer o parser
                documentoHtml = Jsoup.parse(reader.text)
            }
            response.failure = { resp ->
                println "Erro ao acessar página: ${resp.statusLine}"
            }
        }
        return documentoHtml
    }

    static void task1ObterComponenteComunicacao() {
        println "\n[Task 1] Buscando a versão mais recente do Padrão TISS..."

        Document doc = acessarPaginaComHttpBuilder(BASE_URL)

        if (doc == null) {
            println "Falha ao carregar a página base com HTTPBuilder."
            return
        }

        def links = doc.select("a[href]")
        String urlPadraoRecente = null

        // Estratégia 1: Procurar direto no link (href) por algo como "padrao-tiss-mes-ano"
        for (link in links) {
            String href = link.attr("href").toLowerCase()

            // Ignora os links de histórico e tabelas já tratamos nas outras Tasks
            if (href.contains("historico") || href.contains("tabelas")) continue

            // Procura o padrão de URL oficial das atualizações (ex: padrao-tiss-outubro-2024)
            // Filtra anos recentes de 2023 a 2029
            if (href.matches(".*padrao-tiss-[a-z]+-202[3-9].*")) {
                urlPadraoRecente = link.attr("href")
                println "Encontrada a versão mais recente (via URL interna): ${urlPadraoRecente}"
                break
            }
        }

        // Estratégia 2 (Fallback): Procurar pelo texto do link caso a URL seja diferente
        if (!urlPadraoRecente) {
            for (link in links) {
                String text = link.text().toLowerCase()
                if (text.contains("tiss") && text.matches(".*202[3-9].*")) {
                    urlPadraoRecente = link.attr("href")
                    println "Encontrada a versão mais recente (via Texto): ${link.text()}"
                    break
                }
            }
        }

        if (urlPadraoRecente) {
            Document docPadrao = Jsoup.connect(urlPadraoRecente).get()

            // O Componente de Comunicação é SEMPRE distribuído como .zip
            // Jsoup pega links terminados em ".zip"
            def downloadLinkElement = docPadrao.select("a[href\$=.zip]").first()

            // Fallback caso não termine em .zip (redirecionamento)
            if (downloadLinkElement == null) {
                downloadLinkElement = docPadrao.select("a:contains(Comunica), a:contains(Componente)").first()
            }

            if (downloadLinkElement != null) {
                String downloadUrl = downloadLinkElement.attr("href")
                println "Baixando Componente de Comunicação (.zip)..."
                baixarArquivo(downloadUrl, "Componente_de_Comunicacao.zip")
            } else {
                println "ERRO: O Componente de Comunicação não foi encontrado na página."
            }
        } else {
            println "Link da versão mais recente não encontrado. Verifique a estrutura da página da ANS."
        }
    }

    static void task2ObterHistoricoVersoes() {
        println "\n[Task 2] Acessando o histórico de versões..."
        String historyUrl = "${BASE_URL}/padrao-tiss-historico-das-versoes-dos-componentes-do-padrao-tiss"
        Document doc = Jsoup.connect(historyUrl).get()

        def rows = doc.select("table tbody tr")
        File csvFile = new File("${DOWNLOAD_DIR}/historico_tiss.csv")
        csvFile.write("Competencia,Publicacao,Inicio_Vigencia\n")

        Pattern datePattern = Pattern.compile("(?i)(jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez)/(\\d{4})")

        int count = 0
        rows.each { row ->
            def cols = row.select("td")
            if (cols.size() >= 3) {
                String competencia = cols[0].text().trim()
                String publicacao = cols[1].text().trim()
                String vigencia = cols[2].text().trim()

                Matcher matcher = datePattern.matcher(competencia)
                if (matcher.find()) {
                    int ano = matcher.group(2).toInteger()
                    if (ano >= 2016) {
                        csvFile.append("\"${competencia}\",\"${publicacao}\",\"${vigencia}\"\n")
                        count++
                    }
                }
            }
        }
        println "Histórico filtrado (a partir de 2016) concluído! ${count} registros salvos em: historico_tiss.csv"
    }

    static void task3BaixarTabelaErros() {
        println "\n[Task 3] Acessando Tabelas Relacionadas..."
        String urlTabelas = "${BASE_URL}/padrao-tiss-tabelas-relacionadas"
        Document doc = Jsoup.connect(urlTabelas).get()

        def linkErros = doc.select("a:contains(Tabela de erros no envio para a ANS)")
        if (!linkErros.isEmpty()) {
            String downloadUrl = linkErros.attr("href")
            println "Baixando Tabela de Erros..."
            baixarArquivo(downloadUrl, "Tabela_Erros_ANS.xlsx")
        } else {
            println "Link da Tabela de Erros não encontrado."
        }
    }

    static void baixarArquivo(String url, String nomeArquivo) {
        def file = new File("${DOWNLOAD_DIR}/${nomeArquivo}")
        file.withOutputStream { out ->
            new URL(url).withInputStream { inStream ->
                out << inStream
            }
        }
        println "Salvo com sucesso: ${file.path}"
    }
}