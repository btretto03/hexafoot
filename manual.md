# Manual do Jogador — Hexafoot 2026

Guia tela a tela do simulador. Você é o técnico da Seleção Brasileira na Copa de 2026 e precisa levar o time até o hexa.

## 1. Menu principal

Ao abrir o jogo (`./gradlew run`) você cai direto no menu principal, com três opções:

- **Novo jogo:** começa uma campanha do zero, direto na tela de Convocação.
- **Carregar jogo:** abre a tela de carregamento (item 7) e retoma uma campanha salva anteriormente.
- **Sair:** fecha a aplicação.

## 2. Convocação da Seleção Brasileira

Antes de qualquer partida, você precisa fechar a lista oficial de 26 jogadores a partir de uma base de 50 atletas brasileiros.

- A tabela da esquerda mostra os 50 disponíveis; a da direita, os já convocados.
- Use o campo de busca para filtrar por nome.
- Clique num jogador para selecioná-lo (sem precisar segurar Ctrl); clique de novo pra desmarcar. Dá pra selecionar vários de uma vez.
- **Adicionar >>** move os selecionados da base para a convocação; **<< Remover** faz o caminho inverso.
- Atalhos de teclado: Enter ou seta direita convocam o selecionado e voltam o foco pra lista de disponíveis; Backspace ou seta esquerda fazem o caminho inverso na lista de convocados.
- O contador e a barra de progresso no rodapé mostram quantos faltam para os 26.
- O botão **Avançar para o hub** só libera com exatamente 26 jogadores selecionados — tentar avançar com menos ou mais dispara um aviso (é aqui que a exceção `ElencoIncompletoException` entra em ação por trás da tela).

Ao avançar, o jogo monta automaticamente a escalação titular (4-3-3, melhores jogadores por posição) e inicia o torneio.

## 3. Central do Técnico (hub)

É a tela-base entre uma rodada e outra. Nela você vê:

- o resumo do elenco (titulares e banco, com físico, cartões e lesões de cada um);
- o resultado final da campanha, se o Brasil já tiver sido eliminado ou for campeão;
- os atalhos de "Próximos passos":
  - **Abrir escalação e tática** (item 4);
  - **Jogar próxima partida** (só libera quando há uma partida do Brasil agendada; leva direto pra simulação, item 5);
  - **Consultar grupos e mata-mata** (item 6);
  - **Salvar progresso** (grava o save único do jogo; o próprio botão mostra "Progresso salvo!" ou "Erro ao salvar" por alguns segundos);
  - **Voltar ao menu**.

## 4. Escalação e Tática

Aqui você define quem joga e como o time joga.

- O **campo** mostra os 11 titulares organizados por linha (goleiro, defesa, meio, ataque), de acordo com a formação escolhida.
- O **banco** mostra os reservas.
- Pra trocar um jogador de posição: clique nele (ele fica destacado) e depois clique no slot de destino — ou simplesmente arraste e solte. Funciona entre campo↔campo, banco↔banco e campo↔banco.
- Passar o mouse sobre um jogador enquanto outro está selecionado abre um comparativo lado a lado (ataque, defesa, físico, estresse) no painel da direita.
- **Formações:** nove opções (de 3-4-3 a 5-4-1), cada uma muda quantos jogadores entram em cada linha e reorganiza automaticamente quem sobra pro banco.
- **Postura tática:** Pressão (ofensiva), Posse de bola (equilibrada) ou Retranca (defensiva) — cada uma altera os multiplicadores de ataque, defesa e desgaste físico durante a partida.
- **Voltar ao hub** salva as escolhas e retorna à Central do Técnico.

## 5. Simulação da partida

A tela mais "ao vivo" do jogo. Assim que entra, a partida já começa rodando.

- **Placar e cronômetro** no topo.
- **Lances da partida:** log de eventos (gols, cartões, lesões, substituições) atualizado minuto a minuto, com cor por tipo de evento.
- **Seu time em campo** (painel esquerdo): físico e cartões de cada titular, atualizados em tempo real.
- **Controles inferiores:** Pausar, Lento, Normal, Rápido — controlam a velocidade da simulação.
- **Área do técnico** (painel direito, só ativa com o jogo pausado):
  - trocar a postura tática no meio da partida;
  - fazer substituições (escolher quem sai e quem entra, respeitando o limite de 5 trocas e jogadores aptos).
- **Intervalo:** aos 45 minutos o jogo pausa automaticamente e mostra o placar parcial, com a chance de ajustar tática/escalação antes do segundo tempo.
- **Lesão em campo:** se um jogador seu se machuca e há reserva disponível, o jogo trava a simulação até você fazer a substituição obrigatória.
- **Pênaltis:** se o tempo normal termina empatado numa fase eliminatória, a disputa começa direto na mesma tela — você escolhe o batedor a cada cobrança sua, e o resultado (gol ou defesa) aparece com uma pequena pausa de suspense.
- Ao fim da partida (ou dos pênaltis), o botão **Voltar ao Hub** libera e mostra um resumo do resultado, incluindo se a campanha do Brasil terminou ali (eliminado, campeão, vice, etc.).

## 6. Tabelas e Chaveamento

Duas abas:

- **Grupos:** classificação de todos os grupos da Copa (pontos, jogos, vitórias, empates, derrotas, gols pró/contra, saldo), atualizada a cada rodada.
- **Mata-mata:** o chaveamento completo, fase por fase (dezesseis-avos até a final), mostrando os confrontos definidos e os que ainda dependem de vagas em aberto. Depois que o torneio termina, essa aba já abre automaticamente e mostra campeão e terceiro colocado no topo.

## 7. Carregar jogo

Como o jogo trabalha com um único slot de salvamento:

- se existir um save, a tela mostra a data em que foi salvo e o botão **Carregar campanha**;
- se não existir, mostra "Nenhum jogo salvo encontrado";
- carregar com sucesso leva direto pro hub, com a campanha exatamente como estava.

## Dicas rápidas

- Fisico baixo (abaixo de 40%) aumenta risco de lesão e reduz o rendimento — gerencie o desgaste trocando jogadores cansados sempre que possível.
- Cartões amarelos acumulados (2 em partidas distintas) suspendem o jogador automaticamente; cartão vermelho direto também.
- O contador de cartões amarelos zera ao entrar em fases mais avançadas do mata-mata, então cartões antigos não tiram ninguém da decisão.
- Cada seleção adversária também sofre desgaste e lesões nas suas próprias partidas — a CPU simula tudo isso nos bastidores, e você só acompanha diretamente os jogos do Brasil.
