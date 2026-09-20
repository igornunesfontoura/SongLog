# SongLog

Plataforma desktop para catalogar álbuns e avaliar música **faixa por faixa**, gerando a nota média ponderada do disco. Projeto integrador: Java (NetBeans), MySQL, Git/GitHub.

- Documento de projeto: [`docs/documento-de-projeto.md`](docs/documento-de-projeto.md)
- Etapa 4 (banco MySQL, JDBC, testes): [`docs/etapa-4-banco-de-dados.md`](docs/etapa-4-banco-de-dados.md)
- Etapa 3 (telas e interações): [`docs/etapa-3-implementacao.md`](docs/etapa-3-implementacao.md)

## Executar

Precisa de **JDK 17+**, **Maven** e um servidor **MySQL 8.0+** rodando (o mesmo que o MySQL Workbench usa). O Maven baixa o driver JDBC na primeira compilação.

**NetBeans:** *File → Open Project* → esta pasta (projeto Maven) → F6.

**Terminal:**

```bash
mvn package
java -jar target/songlog-0.4.0.jar
```

Se o MySQL pedir senha, o programa abre uma janela de conexão. Ao conectar, ele cria o banco `songlog`, as tabelas e os dados de teste sozinho (scripts em [`banco/`](banco/)) e lembra a conexão em `config/songlog.local.properties` (fora do Git).

Usuário de demonstração: `ana@songlog.dev`, senha `songlog123`.

## Estado atual

Etapa 4: interface Swing completa (login, catálogo, álbum, avaliação, perfil e diário) gravando num banco MySQL via JDBC, com dados iniciais de teste, regras de negócio também no banco (`UNIQUE`, `CHECK`, cascata, views de nota) e 58 verificações automáticas contra um MySQL real.
