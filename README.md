# SongLog

Plataforma desktop para catalogar álbuns e avaliar música **faixa por faixa**, gerando a nota média ponderada do disco. Projeto integrador — Java (NetBeans), MySQL, Git/GitHub.

Documentação completa: [`docs/documento-de-projeto.md`](docs/documento-de-projeto.md)

## Executar

**NetBeans:** *File → Open Project* → selecione esta pasta (projeto Maven) → F6.

**Terminal** (JDK 17+ e Maven):

```bash
mvn package
java -jar target/songlog-0.1.0.jar
```

## Estado atual

Etapa 1: modelo de domínio em memória e protótipo em console (`br.com.songlog.Main`). Sem banco e sem interface ainda.
