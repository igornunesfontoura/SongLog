package br.com.songlog.banco;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Executa um script SQL (os arquivos da pasta banco/) pela conexão JDBC, que não entende um
 * script inteiro de uma vez: separa em comandos, respeitando textos entre aspas e comentários.
 */
final class ScriptSql {

    private ScriptSql() {
    }

    /** Divide o script em comandos, sem o ";" final e sem comentários. */
    static List<String> dividir(String s) {
        List<String> comandos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        char aspas = 0; // ' " ou ` quando estamos dentro de um texto
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            char prox = i + 1 < n ? s.charAt(i + 1) : 0;
            if (aspas == 0) {
                if (c == '-' && prox == '-' && (i + 2 >= n || Character.isWhitespace(s.charAt(i + 2))) || c == '#') {
                    while (i < n && s.charAt(i) != '\n') {
                        i++;
                    }
                    continue;
                }
                if (c == '/' && prox == '*') {
                    int fim = s.indexOf("*/", i + 2);
                    i = fim < 0 ? n : fim + 1;
                    continue;
                }
                if (c == ';') {
                    fechar(comandos, atual);
                    continue;
                }
                if (c == '\'' || c == '"' || c == '`') {
                    aspas = c;
                }
            } else {
                if (c == '\\' && aspas != '`' && i + 1 < n) {
                    atual.append(c).append(s.charAt(++i));
                    continue;
                }
                if (c == aspas) {
                    if (prox == aspas) { // aspa duplicada dentro do texto ('' = uma aspa)
                        atual.append(c).append(prox);
                        i++;
                        continue;
                    }
                    aspas = 0;
                }
            }
            atual.append(c);
        }
        fechar(comandos, atual);
        return comandos;
    }

    private static void fechar(List<String> comandos, StringBuilder atual) {
        String comando = atual.toString().strip();
        if (!comando.isEmpty()) {
            comandos.add(comando);
        }
        atual.setLength(0);
    }

    /**
     * Executa todos os comandos. "USE" e "CREATE DATABASE" são ignorados: o programa já está
     * conectado ao banco certo (esses comandos existem só para quem roda o script no Workbench).
     */
    static void executar(Connection conexao, String script) throws SQLException {
        for (String comando : dividir(script)) {
            String inicio = comando.toUpperCase();
            if (inicio.startsWith("USE ") || inicio.startsWith("CREATE DATABASE")) {
                continue;
            }
            try (Statement st = conexao.createStatement()) {
                st.execute(comando);
            }
        }
    }
}
