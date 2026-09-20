# SongLog

Plataforma desktop para catalogar álbuns e avaliar música **faixa por faixa**, gerando a nota média ponderada do disco. Projeto integrador: Java (NetBeans), MySQL, Git/GitHub.

- Documento de projeto: [`docs/documento-de-projeto.md`](docs/documento-de-projeto.md)
- Etapa 3 (telas e interações, como executar, testes): [`docs/etapa-3-implementacao.md`](docs/etapa-3-implementacao.md)

## Executar

**NetBeans:** *File → Open Project* → esta pasta (projeto Maven) → F6.

**Terminal** (JDK 17+ e Maven):

```bash
mvn package
java -jar target/songlog-0.3.0.jar
```

Usuário de demonstração: `ana@songlog.dev`, senha `songlog123`.

## Estado atual

Etapa 3: interface Swing completa (login, catálogo, álbum, avaliação, perfil e diário) com dados em memória. O acesso ao MySQL entra trocando as implementações de `repositorio`.
