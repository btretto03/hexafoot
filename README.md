# Hexafoot 2026

Hexafoot é um jogo de gerenciamento da Seleção Brasileira na Copa de 2026. O jogador monta a convocação, define titulares, formação e postura tática e acompanha as partidas em uma simulação interativa feita com JavaFX.

## Funcionalidades

- convocação de 26 atletas a partir de uma base de 50 jogadores brasileiros;
- organização de titulares e reservas por clique ou arrastar e soltar;
- nove formações e três posturas táticas, com impacto em ataque, defesa e desgaste;
- Copa completa com 48 seleções, fase de grupos e mata-mata;
- simulação minuto a minuto de gols, cartões, lesões, fadiga e substituições;
- controle de velocidade, pausa, mudanças táticas e substituições durante a partida;
- disputa de pênaltis nas partidas eliminatórias empatadas;
- classificação de grupos, chaveamento e simulação das partidas controladas pela CPU;
- salvamento e carregamento da campanha em um slot local.

## Tecnologias

- Java 21;
- JavaFX 21 (`controls` e `media`);
- Gradle Wrapper;
- JUnit 5.

## Como executar


```bash
cd hexafoot
./gradlew run
```

## Fluxo de uma campanha

1. Inicie um novo jogo e convoque exatamente 26 atletas.
2. Ajuste titulares, banco, formação e postura tática no hub.
3. Jogue a próxima partida do Brasil e acompanhe os eventos em tempo real.
4. Consulte a classificação e o chaveamento entre as rodadas.
5. Salve o progresso para continuar a campanha depois.

O salvamento usa o arquivo `saves/campanha.save`, relativo ao diretório de execução, e substitui o conteúdo do slot anterior.

## Padrões de projeto

O projeto emprega os seguintes padrões:

- **Strategy:** `EstrategiaSimulacao` define os modificadores de ataque, defesa e desgaste. `TaticaOfensiva`, `TaticaEquilibrada` e `TaticaRetranca` podem ser trocadas durante a campanha sem alterar o código dos times ou dos processadores da partida.
- **Observer:** `RelogioPartida` notifica implementações de `ObserverMinuto` a cada passo da simulação. `ProcessadorFisico`, `ProcessadorLesoes`, `ProcessadorCartoes` e `ProcessadorGols` reagem de forma independente ao mesmo minuto.
- **Factory e métodos de fábrica:** `FabricaSelecao` e `FabricaTorneio` concentram a criação dos objetos de domínio a partir dos CSVs. `PartidaTorneio.criarFaseDeGrupos` e `PartidaTorneio.criarEliminatoria` representam as duas formas de criar partidas do calendário.

## Testes e documentação

Execute os testes automatizados com:

```bash
./gradlew test
```

Gere a documentação da API com:

```bash
./gradlew javadoc
```

A documentação gerada fica em `app/build/docs/javadoc/index.html`.
Getters, setters e membros triviais foram omitidos da documentação, pois não se julgou necessário e poluiria o projeto desnecessariamente.

Para compilar o projeto:

```bash
./gradlew build
```

## Arquitetura e estrutura do projeto

A aplicação está dividida por responsabilidades:

- **dados:** lê os recursos CSV, converte os registros em objetos e persiste a campanha;
- **model:** representa jogadores, seleções, partidas, eventos e estados do torneio;
- **service/simulacao:** contém as regras e os processadores executados durante uma partida;
- **service/torneio:** coordena rodadas, classificação, chaveamento, pós-jogo e partidas da CPU;
- **ui:** mantém a sessão compartilhada e controla a navegação;
- **ui/view:** constrói as telas JavaFX e encaminha as ações do usuário aos serviços.

Em linhas gerais, as fábricas carregam os recursos e criam o modelo da competição. Os serviços alteram esse modelo conforme a simulação e a progressão do torneio, enquanto as telas consultam e acionam esses serviços por meio de `GameSession` e `GameNavigator`. O salvamento serializa o estado completo de `GerenciadorTorneio`.

```text
app/src/main/java/hexafoot/
├── dados/                 leitura dos CSVs, fábricas e salvamento
├── model/                 jogadores, times, partidas e estados do torneio
│   └── strategy/          posturas táticas da simulação
├── service/
│   ├── simulacao/         relógio e processadores de eventos da partida
│   └── torneio/           classificação, chaveamento e progressão da Copa
└── ui/                    navegação e sessão compartilhada
    └── view/              telas JavaFX
```

Os elencos, a configuração do torneio, os efeitos sonoros e o estilo visual ficam em `app/src/main/resources`.

## Dados do jogo

Cada elenco é carregado de um CSV com as colunas:

```text
Nome,Clube,Posicao,Ataque,Defesa,Fisico,Estresse
```

Os arquivos em `data/info_torneio` definem os grupos, o calendário das três rodadas e as origens de cada vaga do mata-mata. Alterações nesses arquivos devem preservar os identificadores usados pelo chaveamento.

## Uso de IA

A documentação Javadoc e este README foram gerados e revisados com auxílio de inteligência artificial, conforme previamente autorizado para as atividades. O conteúdo foi conferido em relação ao código e aos comandos do projeto.
