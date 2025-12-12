package com.umari.queueManager.service;

import com.umari.queueManager.Model.TicketHistorico;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvLogService {

    // O arquivo ficará na pasta raiz do projeto (onde rodas o jar)
    private static final String NOME_ARQUIVO = "historico_geral.csv";
    // Usamos ponto e vírgula que é o padrão do Excel no Brasil
    private static final String SEPARADOR = ";";
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void registrarNoArquivo(List<TicketHistorico> novosTickets) {
        File arquivo = new File(NOME_ARQUIVO);
        boolean arquivoNovo = !arquivo.exists();

        // O 'true' no FileWriter significa modo APPEND (adicionar ao final)
        try (FileWriter fw = new FileWriter(arquivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            // Se o arquivo acabou de ser criado, escreve o cabeçalho
            if (arquivoNovo) {
                bw.write("ID" + SEPARADOR +
                        "Senha" + SEPARADOR +
                        "Cliente" + SEPARADOR +
                        "Tipo" + SEPARADOR +
                        "Chegada" + SEPARADOR +
                        "Atendimento/Arquivamento");
                bw.newLine();
            }

            // Escreve cada senha nova linha por linha
            for (TicketHistorico t : novosTickets) {
                String linha = montarLinha(t);
                bw.write(linha);
                bw.newLine();
            }

            System.out.println("📄 CSV Atualizado! " + novosTickets.size() + " linhas adicionadas.");

        } catch (IOException e) {
            System.err.println("❌ Erro ao escrever no CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String montarLinha(TicketHistorico t) {
        // Garante que não dá erro se o nome for nulo
        String nome = (t.getNomeCliente() != null) ? t.getNomeCliente() : "Sem Nome";

        return t.getId() + SEPARADOR +
                t.getNumero() + SEPARADOR +
                nome + SEPARADOR +
                t.getTipo() + SEPARADOR +
                t.getDataCriacao().format(DATA_FMT) + SEPARADOR +
                t.getDataArquivamento().format(DATA_FMT);
    }
}