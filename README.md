# ⚽ Hexafoot 2026

Hexafoot é um jogo de gerenciamento da Seleção Brasileira na Copa de 2026. O jogador monta a convocação, define titulares, formação e postura tática e acompanha as partidas em uma simulação interativa feita com JavaFX. 🇧🇷🏆

## ✨ Funcionalidades

-  convocação de 26 atletas a partir de uma base de 50 jogadores brasileiros, com um botão de **convocação oficial 2026** que seleciona de uma vez os jogadores anunciados por Ancelotti;
-  organização de titulares e reservas por clique ou arrastar e soltar;
-  nove formações e três posturas táticas, com impacto em ataque, defesa e desgaste;
-  Copa completa com 48 seleções, fase de grupos e mata-mata;
-  simulação minuto a minuto de gols, cartões, lesões, fadiga e substituições;
-  controle de velocidade, pausa, mudanças táticas e substituições durante a partida — os avisos do técnico (lesão, troca inválida etc.) aparecem no próprio painel da partida, sem popup travando o jogo;
-  disputa de pênaltis nas partidas eliminatórias empatadas;
-  classificação de grupos, chaveamento e simulação das partidas controladas pela CPU;
-  tela de resultado da campanha do Brasil assim que ela se define — campeão ou eliminado, com som e a opção de simular o restante da Copa até saber quem fica com o título;
-  salvamento e carregamento da campanha em um slot local.

## 🆕 Novidades recentes

- 🏆 **Tela de resultado da campanha:** assim que o destino do Brasil na Copa é selado (título, vice, 3º/4º lugar ou eliminação em qualquer fase), uma tela dedicada aparece com som e o resumo da campanha, além do botão para simular o restante do torneio e descobrir o campeão final.
- 📋 **Convocação oficial 2026:** novo botão na tela de convocação que monta de uma vez os 26 jogadores anunciados oficialmente para a Copa, sem precisar escolher um a um.
- 🐛 **Fim dos popups de lesão/substituição:** os avisos que antes abriam em uma janela separada (e podiam travar a partida) agora aparecem direto no painel do técnico, na mesma tela do jogo.
- 🐛 **Correção no chaveamento:** vencer uma fase do mata-mata — inclusive na disputa de pênaltis — não mostra mais por engano a tela de eliminado.

## 🛠️ Tecnologias

- Java 21;
- JavaFX 21 (`controls` e `media`);
- Gradle Wrapper;
- JUnit 5.

## ▶️ Como executar

```bash
cd hexafoot
./gradlew run
```

## 🗺️ Fluxo de uma campanha

1. Inicie um novo jogo e convoque exatamente 26 atletas (ou use o botão de convocação oficial 2026).
2. Ajuste titulares, banco, formação e postura tática no hub.
3. Jogue a próxima partida do Brasil e acompanhe os eventos em tempo real.
4. Consulte a classificação e o chaveamento entre as rodadas.
5. Salve o progresso para continuar a campanha depois.
6. Quando a participação do Brasil na Copa se encerrar (🏆 campeão ou ❌ eliminado), acompanhe o resultado na tela dedicada e, se quiser, simule o restante do torneio para ver quem levanta a taça.

O salvamento usa o arquivo `saves/campanha.save`, relativo ao diretório de execução, e substitui o conteúdo do slot anterior.

Um roteiro completo, tela a tela, está em [`MANUAL.md`](MANUAL.md).

## 🧩 Conceitos de Orientação a Objetos

Esta seção mapeia os tópicos exigidos pela disciplina (relacionamentos polimórficos, interfaces, classes abstratas e exceções) para onde eles aparecem de fato no código.

### 🔗 Relacionamentos

- **Associação:** `Partida` associa dois objetos `Time` (mandante e visitante) que existem de forma independente da partida — os times continuam existindo antes, durante e depois do confronto. `Time` também se associa a uma `EstrategiaSimulacao`, podendo trocar de tática em tempo real sem que nenhum dos dois lados precise conhecer a classe concreta do outro.
- **Agregação:** `Grupo` agrega uma lista de `Time` (os quatro times do grupo existem independentemente do grupo, que apenas os referencia para calcular a classificação). Da mesma forma, `Time` agrega uma lista de `Jogador`: os atletas são criados pela `FabricaSelecao` a partir do CSV e só depois são distribuídos entre a lista de "disponíveis" (na convocação) e o elenco oficial — o jogador não é criado nem destruído pelo `Time` que o contém no momento.
- **Composição:** `PartidaTorneio` possui uma `Partida` cujo ciclo de vida depende inteiramente dela — o objeto só passa a existir quando `iniciar()` é chamado e representa exclusivamente aquele confronto agendado. Da mesma forma, `Partida` é dona da sua lista de `EventoPartida`: os eventos (gols, cartões, lesões, substituições) são criados e vivem só dentro daquela partida específica.

### 🧬 Herança e polimorfismo

- `TelaBase` (classe abstrata) é estendida por todas as dez telas do jogo (`MainMenuView`, `ConvocacaoView`, `EscalacaoTaticaView`, `HubView`, `SimulacaoPartidaView`, `TabelasChaveamentoView`, `CarregarJogoView`, `FeaturePlaceholderView`, `CalendarioView`, `ResultadoCampanhaView`). O `GameNavigator` troca de tela manipulando sempre a referência polimórfica `ScreenView`, sem precisar saber qual tela concreta está recebendo.
- `ValidadorRegraBase` (classe abstrata) declara o método abstrato `validar()`; `ValidadorConvocacao` e `ValidadorEscalacao` implementam a regra específica de cada etapa, mas são chamados sempre pelo tipo base.
- `TaticaOfensiva`, `TaticaEquilibrada` e `TaticaRetranca` implementam `EstrategiaSimulacao` e são usadas de forma intercambiável por `Time` — a troca de tática no meio da partida não exige nenhum `if`/`else` no motor de simulação, só a substituição do objeto.
- `ProcessadorGols`, `ProcessadorCartoes`, `ProcessadorLesoes` e `ProcessadorFisico` implementam `ObserverMinuto` e são percorridos polimorficamente pelo `RelogioPartida` a cada minuto, sem que o relógio conheça a lógica interna de cada um.

### 🔌 Interfaces (3)

| Interface | Papel |
|---|---|
| `EstrategiaSimulacao` | contrato das táticas (Strategy) |
| `ObserverMinuto` | contrato dos processadores de evento por minuto (Observer) |
| `ScreenView` | contrato mínimo de qualquer tela (`getRoot()`), implementado indiretamente por todas as telas via `TelaBase` |

### 🏗️ Classes abstratas (2)

| Classe abstrata | Papel |
|---|---|
| `TelaBase` | centraliza a referência ao `GameNavigator` que toda tela precisa e formaliza o contrato de `ScreenView` |
| `ValidadorRegraBase` | centraliza a estrutura comum de validação (mensagem de erro + método `validar()` abstrato) usada por `ValidadorConvocacao` e `ValidadorEscalacao` |

### ⚠️ Tratamento de exceções (2)

| Exceção | Quando é lançada |
|---|---|
| `ElencoIncompletoException` | o jogador tenta avançar da convocação sem ter exatamente 26 atletas selecionados |
| `JogadorIndisponivelException` | o jogador tenta escalar como titular um atleta suspenso ou lesionado |

Ambas são exceções checadas (`extends Exception`), então o compilador obriga o tratamento no ponto de chamada, e a interface gráfica usa esse tratamento para mostrar o aviso correspondente ao usuário em vez de deixar o erro estourar.

## 🏛️ Padrões de projeto

- **Strategy:** `EstrategiaSimulacao` define os modificadores de ataque, defesa e desgaste. `TaticaOfensiva`, `TaticaEquilibrada` e `TaticaRetranca` podem ser trocadas durante a campanha sem alterar o código dos times ou dos processadores da partida.
- **Observer:** `RelogioPartida` notifica implementações de `ObserverMinuto` a cada passo da simulação. `ProcessadorFisico`, `ProcessadorLesoes`, `ProcessadorCartoes` e `ProcessadorGols` reagem de forma independente ao mesmo minuto.
- **Factory e métodos de fábrica:** `FabricaSelecao` e `FabricaTorneio` concentram a criação dos objetos de domínio a partir dos CSVs. `PartidaTorneio.criarFaseDeGrupos` e `PartidaTorneio.criarEliminatoria` são métodos de fábrica estáticos que representam as duas formas de criar partidas do calendário, com o construtor mantido privado para forçar o uso deles.
- **Template Method (parcial):** `ValidadorRegraBase` define a estrutura comum de validação e deixa `validar()` como passo variável, implementado por cada subclasse concreta.

## ✅ Testes e documentação

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

## 🏗️ Arquitetura e estrutura do projeto

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
│   ├── strategy/          posturas táticas da simulação
│   └── exception/         exceções de negócio (ElencoIncompletoException, JogadorIndisponivelException)
├── service/
│   ├── simulacao/         relógio, processadores e validadores da partida
│   └── torneio/           classificação, chaveamento e progressão da Copa
└── ui/                    navegação e sessão compartilhada
    └── view/              telas JavaFX (TelaBase + telas concretas)
```

Os elencos, a configuração do torneio, os efeitos sonoros e o estilo visual ficam em `app/src/main/resources`.

## 🗂️ Dados do jogo

Cada elenco é carregado de um CSV com as colunas:

```text
Nome,Clube,Posicao,Ataque,Defesa,Fisico,Estresse
```

Os arquivos em `data/info_torneio` definem os grupos, o calendário das três rodadas e as origens de cada vaga do mata-mata. Alterações nesses arquivos devem preservar os identificadores usados pelo chaveamento.

## 🔄 Mudanças em relação à proposta original (Atividades 2 e 3)

A proposta entregue nas Atividades 2 e 3 previa uma arquitetura ligeiramente diferente em dois pontos. Aqui vai o registro das mudanças e o porquê delas, conforme pedido no critério de "Aderência à Proposta":

- **Camada de persistência simplificada.** O documento original detalhava quatro classes de serviço para salvar/carregar jogo (`GerenciadorPersistencia`, `RepositorioSave`, `SerializadorEstado`, `ArquivoSave`) mais uma interface `Salvavel`. Na implementação, essa camada inteira foi resolvida por uma única classe, `GerenciadorSalvamento`, usando a serialização nativa do Java (`ObjectOutputStream`/`ObjectInputStream`) sobre o próprio `GerenciadorTorneio`. Como o jogo sempre trabalhou com um único slot de save (não múltiplos saves nomeados), a divisão em quatro classes deixou de se justificar — ela existiria só para separar responsabilidades que, nesse escopo, cabem numa classe só sem prejudicar a leitura do código.
- **Terceira interface trocou de papel.** Com a simplificação acima, a interface `Salvavel` (que formalizaria o contrato de "objeto que pode ser salvo") deixou de fazer sentido. O terceiro contrato do projeto, no lugar dela, é `ScreenView` — o contrato mínimo (`getRoot()`) que toda tela do jogo implementa e que o `GameNavigator` usa para trocar de tela de forma polimórfica.
- **`TelaBase` chegou atrasada.** A proposta já prometia `TelaBase` como classe abstrata desde a Atividade 2. Durante boa parte da implementação ela existiu só como a interface `ScreenView`, e cada tela guardava sua própria referência ao `GameNavigator` de forma repetida. Isso foi corrigido depois: `TelaBase` agora existe como classe abstrata (implementando `ScreenView`), centraliza essa referência e é estendida por todas as dez telas — o que também fechou o requisito de "2 classes abstratas" da disciplina (`TelaBase` + `ValidadorRegraBase`).
- **Nome do navegador de telas.** A proposta chamava o controlador central de navegação de `ControladorNavegacao`; na implementação ele se chama `GameNavigator`. A responsabilidade é a mesma (centralizar a troca de telas e o estado de navegação), só o nome mudou.

Fora esses pontos, a implementação segue a proposta: os módulos de convocação, escalação/tática, simulação de partida, gestão do torneio, pós-jogo e persistência existem com praticamente as mesmas classes e responsabilidades descritas nas Atividades 2 e 3.

## 🤖 Uso de IA

A documentação Javadoc, este README, o manual do jogador, a refatoração que introduziu `TelaBase` como classe abstrata, a tela de resultado da campanha, o botão de convocação oficial 2026 e a correção dos popups de lesão/substituição foram feitos com auxílio de inteligência artificial, conforme previamente autorizado para as atividades. O conteúdo foi conferido em relação ao código e aos comandos do projeto.