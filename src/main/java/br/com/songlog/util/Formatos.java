package br.com.songlog.util;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Formatos pt-BR da interface: vírgula decimal, datas dd/MM/aaaa e meses por extenso.
 */
public final class Formatos {

    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu", PT_BR);
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MMMM uuuu", PT_BR);
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM", PT_BR);

    private Formatos() {
    }

    /** Nota de faixa: uma casa decimal (4,5). */
    public static String nota1(double valor) {
        return String.format(PT_BR, "%.1f", valor);
    }

    /** Nota de álbum: duas casas decimais (4,56). */
    public static String nota2(double valor) {
        return String.format(PT_BR, "%.2f", valor);
    }

    public static String data(LocalDate data) {
        return DATA.format(data);
    }

    public static String diaMes(LocalDate data) {
        return DIA_MES.format(data);
    }

    /** "SETEMBRO 2026". */
    public static String mesAno(LocalDate data) {
        return MES_ANO.format(data).toUpperCase(PT_BR);
    }

    /** Interpreta dd/MM/aaaa (estrito). */
    public static LocalDate interpretarData(String texto) {
        try {
            return LocalDate.parse(texto == null ? "" : texto.strip(), DATA.withResolverStyle(java.time.format.ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("use o formato dd/mm/aaaa (ex.: 20/09/2026).");
        }
    }

    /** Minúsculas e sem acentos, para busca tolerante. */
    public static String semAcentos(String texto) {
        String n = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").toLowerCase(PT_BR);
    }
}
