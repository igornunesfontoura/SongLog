# SongLog

Aplicação desktop para catalogar álbuns e avaliar música **faixa por faixa**. A nota do álbum é calculada a partir das notas de cada faixa, com mais peso para as faixas mais longas. Inspirada no Letterboxd.

Nasceu como Projeto Integrador do curso Técnico em Desenvolvimento de Sistemas do Senac.

<!-- DEMO: substitua a linha abaixo por um GIF mostrando uma avaliação faixa por faixa -->
<p align="center"><em>🎬 GIF de demonstração em breve</em></p>

<p align="center">
  <img src="docs/telas/04-avaliacao.png" alt="Tela de avaliação do SongLog, com nota por faixa e nota ponderada do álbum" width="720">
</p>

## Por que existe

O SongLog serve a quem gosta de música e quer registrar o que ouve com mais precisão do que uma nota única por álbum permite. Nas plataformas comuns, um disco com três obras-primas e seis faixas de preenchimento recebe a mesma nota de um disco regular do começo ao fim. No SongLog, o usuário avalia **cada faixa**, e o sistema calcula a nota do álbum dando mais peso às faixas mais longas. Cada usuário também mantém um **diário de audição**, um registro cronológico do que ouviu e quando.

## Status do projeto

**Em desenvolvimento.** Login, catálogo, avaliação, perfil e diário já funcionam e gravam os dados em um banco MySQL. O projeto continua recebendo ajustes e melhorias.

## Funcionalidades

| Cód. | Requisito |
|---|---|
| RF01 | Cadastrar-se e autenticar-se com e-mail e senha (a senha é guardada só como hash) |
| RF02 | Cadastrar álbum com título, artista, ano e gênero |
| RF03 | Cadastrar, editar e remover as faixas de um álbum (título e duração) |
| RF04 | Listar e buscar álbuns por título, artista ou gênero |
| RF05 | Avaliar cada faixa com nota de 0,5 a 5,0 e comentário opcional |
| RF06 | Exibir a nota ponderada do álbum para o usuário, a indicação de avaliação parcial e a nota geral de todos os usuários |
| RF07 | Registrar uma audição no diário (álbum, data e comentário) |
| RF08 | Listar o diário em ordem cronológica inversa |
| RF09 | Exibir o perfil: álbuns ouvidos, faixas avaliadas, nota média dada e últimas avaliações |
| RF10 | Editar e excluir as próprias avaliações e entradas do diário |

A interface também oferece três temas (claro, escuro e alto contraste), texto ampliável até 150% e navegação por teclado.

## Tecnologias

| Tecnologia | Uso no projeto |
|---|---|
| **Java 17** | linguagem de toda a aplicação |
| **Java Swing** | interface gráfica desktop |
| **MySQL 8** | banco de dados |
| **MySQL Workbench** | modelagem e consulta ao banco (scripts em [`banco/`](banco/)) |
| **JDBC** (MySQL Connector/J) | conexão do Java com o MySQL |
| **NetBeans** (projeto Maven) | ambiente de desenvolvimento e execução |
| **Figma** | projeto das interfaces (wireframes e guia de estilo, na pasta [`design/`](design/)) |
| **Git e GitHub** | versionamento e compartilhamento do código |

## Requisitos

- **JDK 17** ou mais novo
- **Maven** (o NetBeans já traz um embutido)
- **MySQL 8.0.16** ou mais novo. As restrições `CHECK` do esquema exigem essa versão. MariaDB não foi testado.
- Internet na primeira compilação, para o Maven baixar o driver JDBC

## Configurando o MySQL

O SongLog cria sozinho o banco `songlog`, as tabelas e os dados de teste na primeira execução. Você só precisa de um servidor MySQL rodando e de um usuário com permissão.

### 1. Ter um MySQL rodando

Use a instalação que você já tem (a mesma do MySQL Workbench) ou suba uma com Docker:

```bash
docker run -d --name songlog-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=troque-esta-senha mysql:8.0
```

### 2. (Opcional) Criar um usuário só para o SongLog

O padrão é conectar como `root`. Se preferir um usuário próprio, rode no Workbench ou no cliente `mysql`:

```sql
CREATE USER 'songlog'@'localhost' IDENTIFIED BY 'troque-esta-senha';
GRANT ALL PRIVILEGES ON songlog.* TO 'songlog'@'localhost';
```

Se o MySQL estiver no Docker, use `'%'` no lugar de `'localhost'`.

### 3. Informar a conexão

Na primeira execução, o programa tenta `localhost:3306` com o usuário `root` e senha vazia. Se não conseguir, abre a janela **Conexão com o MySQL**, onde você informa servidor, porta, usuário e senha. A conexão que funcionar fica salva em `config/songlog.local.properties`, arquivo que não vai para o Git.

Também dá para configurar sem a janela. Cada fonte abaixo sobrepõe a anterior:

1. `config/songlog.properties`: valores padrão do projeto (host, porta, nome do banco, usuário)
2. `config/songlog.local.properties`: criado pela janela de conexão
3. Variáveis de ambiente: `SONGLOG_DB_HOST`, `SONGLOG_DB_PORTA`, `SONGLOG_DB_NOME`, `SONGLOG_DB_USUARIO`, `SONGLOG_DB_SENHA`
4. Propriedades na linha de comando: `-Dsonglog.db.host=...`, `-Dsonglog.db.usuario=...` etc.

Para não carregar os dados de teste, use `banco.dados-iniciais=false` (ou `SONGLOG_DB_DADOS_INICIAIS=false`).

### Criar o banco pelo Workbench (alternativa)

Se preferir ver ou montar o banco você mesmo, abra os scripts em *File → Open SQL Script* e execute na ordem:

1. [`banco/01-esquema.sql`](banco/01-esquema.sql): banco e tabelas
2. [`banco/02-dados-iniciais.sql`](banco/02-dados-iniciais.sql): dados de teste

Os dois podem ser executados mais de uma vez sem apagar dados. O diagrama do banco está em [`banco/diagrama-er.png`](banco/diagrama-er.png).

## Instalação e uso

```bash
git clone https://github.com/igornunesfontoura/SongLog.git
cd SongLog
mvn package
java -jar target/songlog-0.4.0.jar
```

Rode o `java -jar` de dentro da pasta do projeto, porque o programa procura a configuração em `config/`.

No NetBeans: *File → Open Project*, escolha a pasta `SongLog` e aperte **F6**.

Com os dados de teste carregados, entre com o usuário de demonstração:

- E-mail: `ana@songlog.dev`
- Senha: `songlog123`

Ou crie a sua conta na tela de login.

## Estrutura do repositório

| Pasta | Conteúdo |
|---|---|
| `src/main/java/br/com/songlog` | código: `model`, `service`, `repositorio`, `banco`, `view`, `util` |
| `src/test/java` | testes de interação e geração de capturas de tela |
| `banco/` | scripts SQL (esquema, dados iniciais, consultas de exemplo) e diagrama ER |
| `config/` | configuração padrão da conexão com o MySQL |
| `docs/` | documento de projeto, diagrama de classes, documentação de cada etapa e capturas de tela |
| `design/` | projeto de interfaces (Etapa 2) |

Documentação: [documento de projeto](docs/documento-de-projeto.md) · [Etapa 3: telas](docs/etapa-3-implementacao.md) · [Etapa 4: banco de dados](docs/etapa-4-banco-de-dados.md)

## Time de desenvolvedores

| Nome | GitHub |
|---|---|
| Igor Nunes Fontoura | [@igornunesfontoura](https://github.com/igornunesfontoura) |
