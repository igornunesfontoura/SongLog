# SongLog

Aplicação desktop para catalogar álbuns e avaliar música **faixa por faixa**, gerando a nota média do disco ponderada pela duração de cada faixa. Inspirada no Letterboxd. Projeto Integrador (PI) de faculdade.

## Status do projeto

**Em desenvolvimento.** Já funcionam login, catálogo, avaliação, perfil e diário, gravando em um banco MySQL. O projeto continua recebendo ajustes e melhorias.

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

## Time de desenvolvedores

| Nome | GitHub |
|---|---|
| Igor Nunes Fontoura | [@igornunesfontoura](https://github.com/igornunesfontoura) |

## Objetivo do software

O SongLog serve a quem gosta de música e quer registrar o que ouve com mais precisão do que uma nota única por álbum permite. Um disco com três obras-primas e seis faixas de preenchimento recebe, nas plataformas comuns, a mesma nota de um disco regular do começo ao fim. No SongLog o usuário avalia **cada faixa** e o sistema calcula a nota do álbum, dando mais peso às faixas mais longas. Cada usuário também mantém um **diário de audição**, um registro cronológico do que ouviu e quando.

## Funcionalidades do sistema (requisitos)

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

Além disso, a interface oferece três temas (claro, escuro e alto contraste), texto ampliável até 150% e navegação por teclado.

## Como executar

Precisa de **JDK 17+**, **Maven** e um servidor **MySQL 8.0+** rodando. O Maven baixa o driver JDBC na primeira compilação.

```bash
git clone https://github.com/igornunesfontoura/SongLog.git
cd SongLog
mvn package
java -jar target/songlog-0.4.0.jar
```

No NetBeans: *File → Open Project* → pasta `SongLog` → **F6**.

Se o MySQL exigir senha, o programa abre uma janela de conexão. Ao conectar, ele cria o banco `songlog`, as tabelas e os dados de teste sozinho e lembra a conexão em `config/songlog.local.properties` (fora do Git). Usuário de demonstração: `ana@songlog.dev`, senha `songlog123`.

## Estrutura do repositório

| Pasta | Conteúdo |
|---|---|
| `src/main/java/br/com/songlog` | código: `model`, `service`, `repositorio`, `banco`, `view`, `util` |
| `src/test/java` | testes de interação e geração de capturas de tela |
| `banco/` | scripts SQL (esquema, dados iniciais, consultas de exemplo) e diagrama ER |
| `config/` | configuração padrão da conexão com o MySQL |
| `docs/` | documento de projeto, diagrama de classes e documentação de cada etapa |
| `design/` | projeto de interfaces (Etapa 2) |

Documentação: [documento de projeto](docs/documento-de-projeto.md) · [Etapa 4: banco de dados](docs/etapa-4-banco-de-dados.md) · [Etapa 3: telas](docs/etapa-3-implementacao.md)
