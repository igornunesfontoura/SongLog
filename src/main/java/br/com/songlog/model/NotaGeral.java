package br.com.songlog.model;

/** Nota geral do álbum: média das notas dos usuários que avaliaram todas as faixas (RN07). */
public record NotaGeral(double valor, int totalAvaliadores) {
}
