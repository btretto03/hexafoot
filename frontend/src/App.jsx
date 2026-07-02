import { useEffect, useRef, useState } from 'react';

const tactics = ['Equilibrada', 'Ofensiva', 'Retranca'];
const lineupPositions = [
  'Goleiro',
  'Lateral esquerdo',
  'Zagueiro',
  'Zagueiro',
  'Lateral direito',
  'Meia esquerdo',
  'Meia central',
  'Meia central',
  'Meia direito',
  'Atacante esquerdo',
  'Atacante',
  'Atacante direito',
];

const playerAttributes = {
  Alisson: { position: 'Goleiro', pace: 88, shooting: 70, passing: 82, defense: 86, overall: 90 },
  Ederson: { position: 'Goleiro', pace: 90, shooting: 68, passing: 88, defense: 83, overall: 90 },
  Bento: { position: 'Goleiro', pace: 86, shooting: 66, passing: 79, defense: 80, overall: 84 },
  'Dani Alves': { position: 'Lateral', pace: 84, shooting: 78, passing: 80, defense: 76, overall: 82 },
  Danilo: { position: 'Lateral', pace: 82, shooting: 74, passing: 77, defense: 82, overall: 84 },
  'Alex Sandro': { position: 'Lateral', pace: 80, shooting: 72, passing: 77, defense: 82, overall: 82 },
  'Renan Lodi': { position: 'Lateral', pace: 83, shooting: 70, passing: 76, defense: 79, overall: 81 },
  Marquinhos: { position: 'Zagueiro', pace: 74, shooting: 68, passing: 78, defense: 90, overall: 89 },
  'Thiago Silva': { position: 'Zagueiro', pace: 68, shooting: 62, passing: 74, defense: 91, overall: 86 },
  'Éder Militão': { position: 'Zagueiro', pace: 82, shooting: 70, passing: 76, defense: 87, overall: 87 },
  'Gabriel Magalhães': { position: 'Zagueiro', pace: 74, shooting: 64, passing: 74, defense: 86, overall: 85 },
  Bremer: { position: 'Zagueiro', pace: 76, shooting: 66, passing: 71, defense: 87, overall: 84 },
  Casemiro: { position: 'Meio', pace: 70, shooting: 72, passing: 80, defense: 90, overall: 88 },
  Fabinho: { position: 'Meio', pace: 72, shooting: 74, passing: 82, defense: 86, overall: 86 },
  'Bruno Guimarães': { position: 'Meio', pace: 75, shooting: 74, passing: 84, defense: 84, overall: 86 },
  'Lucas Paquetá': { position: 'Meio', pace: 78, shooting: 80, passing: 84, defense: 76, overall: 85 },
  'Éverton Ribeiro': { position: 'Meio', pace: 77, shooting: 78, passing: 83, defense: 72, overall: 82 },
  'Philippe Coutinho': { position: 'Meio', pace: 76, shooting: 82, passing: 85, defense: 70, overall: 84 },
  Neymar: { position: 'Atacante', pace: 90, shooting: 88, passing: 90, defense: 68, overall: 92 },
  'Vinícius Jr.': { position: 'Atacante', pace: 96, shooting: 86, passing: 82, defense: 72, overall: 91 },
  Rodrygo: { position: 'Atacante', pace: 90, shooting: 84, passing: 82, defense: 70, overall: 87 },
  Raphinha: { position: 'Atacante', pace: 89, shooting: 83, passing: 80, defense: 74, overall: 86 },
  Richarlison: { position: 'Atacante', pace: 84, shooting: 85, passing: 76, defense: 72, overall: 84 },
  'Gabriel Jesus': { position: 'Atacante', pace: 85, shooting: 82, passing: 78, defense: 74, overall: 84 },
  Pedro: { position: 'Atacante', pace: 82, shooting: 80, passing: 76, defense: 70, overall: 82 },
  Antony: { position: 'Atacante', pace: 88, shooting: 80, passing: 77, defense: 68, overall: 83 },
  Martinelli: { position: 'Atacante', pace: 86, shooting: 81, passing: 74, defense: 72, overall: 83 },
  Willian: { position: 'Atacante', pace: 84, shooting: 79, passing: 78, defense: 69, overall: 81 },
  'David Neres': { position: 'Atacante', pace: 87, shooting: 79, passing: 80, defense: 69, overall: 82 },
  'Matheus Cunha': { position: 'Atacante', pace: 85, shooting: 81, passing: 74, defense: 70, overall: 82 },
  Gabigol: { position: 'Atacante', pace: 80, shooting: 84, passing: 72, defense: 64, overall: 80 },
  'Tiquinho Soares': { position: 'Atacante', pace: 79, shooting: 82, passing: 70, defense: 64, overall: 80 },
  Gerson: { position: 'Meio', pace: 78, shooting: 74, passing: 79, defense: 74, overall: 80 },
  'Andreas Pereira': { position: 'Meio', pace: 78, shooting: 76, passing: 81, defense: 72, overall: 80 },
  Fred: { position: 'Meio', pace: 73, shooting: 74, passing: 79, defense: 76, overall: 79 },
  Hulk: { position: 'Atacante', pace: 81, shooting: 84, passing: 76, defense: 66, overall: 82 },
  'Douglas Luiz': { position: 'Meio', pace: 74, shooting: 76, passing: 81, defense: 79, overall: 81 },
  Joelinton: { position: 'Meio', pace: 78, shooting: 76, passing: 73, defense: 79, overall: 80 },
  'Renato Augusto': { position: 'Meio', pace: 74, shooting: 76, passing: 82, defense: 76, overall: 81 },
  'Luiz Henrique': { position: 'Atacante', pace: 88, shooting: 79, passing: 77, defense: 69, overall: 82 },
  Endrick: { position: 'Atacante', pace: 89, shooting: 81, passing: 77, defense: 66, overall: 83 },
  'Vitor Roque': { position: 'Atacante', pace: 87, shooting: 82, passing: 74, defense: 68, overall: 83 },
  Savinho: { position: 'Atacante', pace: 88, shooting: 78, passing: 79, defense: 68, overall: 82 },
  'Júlio César': { position: 'Goleiro', pace: 84, shooting: 60, passing: 76, defense: 78, overall: 80 },
  'Diego Carlos': { position: 'Zagueiro', pace: 72, shooting: 64, passing: 72, defense: 84, overall: 82 },
  Arana: { position: 'Lateral', pace: 82, shooting: 72, passing: 77, defense: 74, overall: 80 },
  'Ayrton Lucas': { position: 'Lateral', pace: 82, shooting: 68, passing: 74, defense: 78, overall: 80 },
  'Léo Ortiz': { position: 'Zagueiro', pace: 74, shooting: 64, passing: 72, defense: 83, overall: 81 },
  'Fábio Santos': { position: 'Lateral', pace: 76, shooting: 66, passing: 77, defense: 78, overall: 79 },
  Marlon: { position: 'Lateral', pace: 80, shooting: 68, passing: 73, defense: 78, overall: 79 },
};

const brazilPlayers = Object.keys(playerAttributes);

const opponents = [
  {
    name: 'Argentina',
    flag: '🇦🇷',
    coach: 'Scaloni',
    strength: 0.92,
    formation: '4-4-2',
    roster: ['Martínez', 'Tagliafico', 'Otamendi', 'Molina', 'Paredes', 'Enzo', 'De Paul', 'Di María', 'Messi', 'Lautaro', 'Álvarez'],
  },
  {
    name: 'França',
    flag: '🇫🇷',
    coach: 'Deschamps',
    strength: 0.88,
    formation: '4-2-3-1',
    roster: ['Maignan', 'Koundé', 'Varane', 'Dayot', 'Theo', 'Camavinga', 'Rabiot', 'Mbappé', 'Griezmann', 'Ousmane', 'Giroud'],
  },
  {
    name: 'Alemanha',
    flag: '🇩🇪',
    coach: 'Nagelsmann',
    strength: 0.86,
    formation: '4-3-3',
    roster: ['Neuer', 'Kimmich', 'Tah', 'Rudiger', 'Müller', 'Gundogan', 'Andrich', 'Sane', 'Musiala', 'Havertz', 'Werner'],
  },
];

const buildLineupFromPlayers = (players) =>
  lineupPositions.reduce((accumulator, position, index) => {
    accumulator[position] = players[index] ?? '';
    return accumulator;
  }, {});

const buildInitialStatuses = (players) =>
  players.reduce((accumulator, player) => {
    const base = Math.max(72, Math.min(98, (playerAttributes[player]?.overall ?? 80) + 8));
    accumulator[player] = { stamina: base, status: base >= 85 ? 'Bom' : base >= 70 ? 'Cansado' : 'Leve' };
    return accumulator;
  }, {});

const getPlayerStatus = (stamina) => {
  if (stamina >= 85) return 'Bom';
  if (stamina >= 70) return 'Cansado';
  return 'Leve';
};

function App() {
  const [screen, setScreen] = useState('menu');
  const [selectedSquad, setSelectedSquad] = useState([]);
  const [lineup, setLineup] = useState({});
  const [tactic, setTactic] = useState('Equilibrada');
  const [fixtureIndex, setFixtureIndex] = useState(0);
  const [result, setResult] = useState({
    home: 0,
    away: 0,
    event: 'Convocação ainda não confirmada',
    minute: '0\'',
    possession: { home: 50, away: 50 },
    shots: { home: 0, away: 0 },
    highlights: ['Escolha a delegação e monte a equipe'],
  });
  const [matchStatus, setMatchStatus] = useState('Escolha os 26 jogadores da sua seleção para a Copa');
  const [summary, setSummary] = useState([]);
  const [selectedPlayer, setSelectedPlayer] = useState(null);
  const [benchPlayers, setBenchPlayers] = useState([]);
  const [liveMinute, setLiveMinute] = useState(0);
  const [isMatchLive, setIsMatchLive] = useState(false);
  const [playerStatuses, setPlayerStatuses] = useState({});
  const [substitutionTargets, setSubstitutionTargets] = useState({});

  const currentOpponent = opponents[fixtureIndex] ?? opponents[opponents.length - 1];
  const isTournamentActive = screen === 'match' || screen === 'finished';
  const playerStatusesRef = useRef(playerStatuses);
  const lineupRef = useRef(lineup);

  useEffect(() => {
    if (selectedSquad.length > 0) {
      setLineup(buildLineupFromPlayers(selectedSquad));
    }
  }, [selectedSquad]);

  useEffect(() => {
    playerStatusesRef.current = playerStatuses;
  }, [playerStatuses]);

  useEffect(() => {
    lineupRef.current = lineup;
  }, [lineup]);

  useEffect(() => {
    if (!isMatchLive) return undefined;
    if (liveMinute >= 90) {
      setIsMatchLive(false);
      setMatchStatus('Partida encerrada. Acompanhe o resultado final.');
      return undefined;
    }

    const timer = window.setInterval(() => {
      setLiveMinute((currentMinute) => {
        const nextMinute = currentMinute + 1;
        const minuteLabel = `${nextMinute}'`;
        const starters = Object.values(lineupRef.current).filter(Boolean);
        const averageStamina = starters.length > 0
          ? starters.reduce((sum, player) => sum + (playerStatusesRef.current[player]?.stamina ?? 90), 0) / starters.length
          : 90;
        const goalChance = 0.04 + (tactic === 'Ofensiva' ? 0.012 : tactic === 'Retranca' ? -0.008 : 0.004) + (nextMinute > 60 ? 0.008 : 0);
        const opponentChance = 0.03 + currentOpponent.strength * 0.01 + (nextMinute > 60 ? 0.008 : 0);
        const homeGoal = Math.random() < goalChance * (averageStamina > 80 ? 1.08 : 0.95) ? 1 : 0;
        const awayGoal = Math.random() < opponentChance ? 1 : 0;
        const eventText = homeGoal > awayGoal
          ? `Gol do Brasil aos ${minuteLabel}`
          : awayGoal > homeGoal
            ? `Gol de ${currentOpponent.name} aos ${minuteLabel}`
            : `Aos ${minuteLabel}, a jogada foi interrompida no meio`;

        setResult((currentResult) => ({
          ...currentResult,
          home: currentResult.home + homeGoal,
          away: currentResult.away + awayGoal,
          event: eventText,
          minute: minuteLabel,
          possession: {
            home: Math.max(30, Math.min(70, currentResult.possession.home + (homeGoal ? 3 : -1) + (tactic === 'Ofensiva' ? 2 : 0))),
            away: 100 - Math.max(30, Math.min(70, currentResult.possession.home + (homeGoal ? 3 : -1) + (tactic === 'Ofensiva' ? 2 : 0))),
          },
          shots: {
            home: currentResult.shots.home + (homeGoal ? 2 : 1),
            away: currentResult.shots.away + (awayGoal ? 2 : 1),
          },
          highlights: [eventText, ...(currentResult.highlights || []).slice(0, 2)],
        }));

        setPlayerStatuses((currentStatuses) => {
          const nextStatuses = { ...currentStatuses };
          starters.forEach((player) => {
            const existing = nextStatuses[player] ?? { stamina: 90, status: 'Bom' };
            const staminaLoss = 2 + (Math.random() < 0.28 ? 4 : 0) + (tactic === 'Retranca' ? 1 : 0);
            const nextStamina = Math.max(0, existing.stamina - staminaLoss);
            nextStatuses[player] = {
              ...existing,
              stamina: nextStamina,
              status: getPlayerStatus(nextStamina),
            };
          });
          return nextStatuses;
        });

        if (nextMinute >= 90) {
          setIsMatchLive(false);
          setMatchStatus('Partida encerrada. Confira o resultado e as substituições.');
        } else {
          setMatchStatus(`Minuto ${minuteLabel}: ${eventText}`);
        }

        return nextMinute;
      });
    }, 1000);

    return () => window.clearInterval(timer);
  }, [isMatchLive, tactic, currentOpponent]);

  const togglePlayer = (player) => {
    if (selectedSquad.includes(player)) {
      setSelectedSquad((current) => current.filter((entry) => entry !== player));
      return;
    }

    if (selectedSquad.length >= 26) {
      setMatchStatus('A delegação já chegou a 26 jogadores. Remova alguém para incluir outro.');
      return;
    }

    setSelectedSquad((current) => [...current, player]);
  };

  const handleLineupChange = (position, player) => {
    setLineup((current) => ({ ...current, [position]: player }));
  };

  const confirmSquad = () => {
    if (selectedSquad.length < 18) {
      setMatchStatus('Convocação incompleta. Selecione ao menos 18 jogadores para iniciar a Copa.');
      return;
    }

    const starterLineup = buildLineupFromPlayers(selectedSquad);
    const starters = Object.values(starterLineup).filter(Boolean);
    const bench = selectedSquad.filter((player) => !starters.includes(player));

    setLineup(starterLineup);
    setBenchPlayers(bench);
    setPlayerStatuses(buildInitialStatuses(selectedSquad));
    setLiveMinute(0);
    setIsMatchLive(false);
    setSubstitutionTargets({});
    setScreen('match');
    setMatchStatus('Convocação confirmada. O primeiro jogo já pode acontecer.');
    setResult({
      home: 0,
      away: 0,
      event: 'Aguardando primeiro jogo',
      minute: '0\'',
      possession: { home: 50, away: 50 },
      shots: { home: 0, away: 0 },
      highlights: ['Seleção definida', 'Ajuste a tática e simule o jogo'],
    });
  };

  const simulateFixture = () => {
    if (selectedSquad.length < 18) {
      setMatchStatus('Primeiro confirme a convocação antes de jogar.');
      return;
    }

    if (isMatchLive) {
      setIsMatchLive(false);
      setMatchStatus('Partida pausada. Você pode fazer substituições e retomar depois.');
      return;
    }

    if (liveMinute >= 90) {
      setLiveMinute(0);
      setResult({
        home: 0,
        away: 0,
        event: 'Nova partida iniciada',
        minute: '0\'',
        possession: { home: 50, away: 50 },
        shots: { home: 0, away: 0 },
        highlights: ['Partida reiniciada', 'Acompanhe os minutos e faça substituições'],
      });
    }

    setIsMatchLive(true);
    setMatchStatus('Partida em andamento. Acompanhe os minutos e faça substituições quando necessário.');
  };

  const handleSubstitution = (position, replacement) => {
    const currentPlayer = lineup[position];
    if (!replacement || !currentPlayer || replacement === currentPlayer) return;

    setLineup((currentLineup) => ({ ...currentLineup, [position]: replacement }));
    setBenchPlayers((currentBench) => {
      const nextBench = currentBench.filter((player) => player !== replacement);
      if (currentPlayer && !nextBench.includes(currentPlayer)) {
        nextBench.push(currentPlayer);
      }
      return nextBench;
    });
    setSubstitutionTargets((currentTargets) => ({ ...currentTargets, [position]: '' }));
    setMatchStatus(`${replacement} entrou para ${position} no lugar de ${currentPlayer}.`);
  };

  const advanceFixture = () => {
    if (fixtureIndex < opponents.length - 1) {
      setFixtureIndex((current) => current + 1);
      setMatchStatus(`Próximo jogo: Brasil x ${opponents[fixtureIndex + 1].name}`);
      setResult({
        home: 0,
        away: 0,
        event: 'Próximo jogo disponível',
        minute: '0\'',
        possession: { home: 50, away: 50 },
        shots: { home: 0, away: 0 },
        highlights: ['Aguardando novo confronto'],
      });
      return;
    }

    setScreen('finished');
    setMatchStatus('Copa encerrada. Sua campanha foi simulada.');
  };

  const resetTournament = () => {
    setScreen('menu');
    setSelectedSquad([]);
    setLineup({});
    setBenchPlayers([]);
    setFixtureIndex(0);
    setSummary([]);
    setLiveMinute(0);
    setIsMatchLive(false);
    setPlayerStatuses({});
    setSubstitutionTargets({});
    setMatchStatus('Escolha os 26 jogadores da sua seleção para a Copa');
    setResult({
      home: 0,
      away: 0,
      event: 'Convocação ainda não confirmada',
      minute: '0\'',
      possession: { home: 50, away: 50 },
      shots: { home: 0, away: 0 },
      highlights: ['Escolha a delegação e monte a equipe'],
    });
  };

  return (
    <div className="app-shell">
      {screen === 'menu' ? (
        <section className="hero-card card">
          <div className="hero-copy">
            <p className="eyebrow">Hexafoot</p>
            <h1>Gerencie o Brasil na Copa do Mundo</h1>
            <p className="subtitle">Escolha a delegação, monte a escalação e simule os jogos com você no comando do time canarinho.</p>
          </div>
          <div className="hero-actions">
            <button onClick={() => setScreen('squad')}>Montar convocação</button>
            <button className="ghost" onClick={() => setScreen('squad')}>Começar Copa</button>
          </div>
          <p className="helper-text">Você controla apenas o Brasil. Os outros times aparecem com escalações fixas.</p>
        </section>
      ) : null}

      {screen === 'squad' ? (
        <section className="card panel-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Convocação da Copa</p>
              <h2>Escolha os 26 jogadores do Brasil</h2>
              <p className="subtitle">Você decide quem entra na lista e, em seguida, define a escalação titular.</p>
            </div>
            <div className="pill">{selectedSquad.length}/26 jogadores</div>
          </div>

          <div className="summary-strip">
            <div className="summary-pill">
              <span className="summary-label">Convocados</span>
              <strong>{selectedSquad.length}</strong>
            </div>
            <div className="summary-pill">
              <span className="summary-label">Titulares</span>
              <strong>{Object.values(lineup).filter(Boolean).length}</strong>
            </div>
            <div className="summary-pill">
              <span className="summary-label">Tática</span>
              <strong>{tactic}</strong>
            </div>
          </div>

          <div className="attribute-panel sticky-preview">
            {selectedPlayer ? (
              <>
                <div className="section-heading compact">
                  <div>
                    <h3>{selectedPlayer}</h3>
                    <p className="subtitle">{playerAttributes[selectedPlayer]?.position}</p>
                  </div>
                  <div className="pill">Overall {playerAttributes[selectedPlayer]?.overall}</div>
                </div>
                <div className="attribute-grid">
                  {[
                    ['Pace', playerAttributes[selectedPlayer]?.pace],
                    ['Chute', playerAttributes[selectedPlayer]?.shooting],
                    ['Passe', playerAttributes[selectedPlayer]?.passing],
                    ['Defesa', playerAttributes[selectedPlayer]?.defense],
                  ].map(([label, value]) => (
                    <div key={label} className="attribute-card">
                      <div className="attribute-card-header">
                        <span>{label}</span>
                        <strong>{value}</strong>
                      </div>
                      <div className="attribute-meter">
                        <div className="attribute-meter-fill" style={{ width: `${value}%` }} />
                      </div>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <p className="helper-text">Passe o mouse ou toque em um jogador para ver seus atributos antes de convocá-lo.</p>
            )}
          </div>

          <div className="player-grid">
            {brazilPlayers.map((player) => {
              const active = selectedSquad.includes(player);
              const attributes = playerAttributes[player];
              return (
                <button
                  key={player}
                  className={`player-chip ${active ? 'active' : ''}`}
                  onMouseEnter={() => setSelectedPlayer(player)}
                  onFocus={() => setSelectedPlayer(player)}
                  onClick={() => {
                    setSelectedPlayer(player);
                    togglePlayer(player);
                  }}
                >
                  <div className="player-chip-top">
                    <span className="player-chip-status">{active ? '✓' : '+'}</span>
                    <strong>{player}</strong>
                  </div>
                  <small>{attributes?.position}</small>
                  <div className="player-chip-stats">
                    <span>OVR {attributes?.overall}</span>
                    <span>PA {attributes?.pace}</span>
                    <span>CH {attributes?.shooting}</span>
                  </div>
                </button>
              );
            })}
          </div>

          <div className="lineup-card">
            <div className="section-heading compact">
              <div>
                <h3>Escalação titular</h3>
                <p className="subtitle">Ajuste os 11 iniciais usando apenas os jogadores já escolhidos.</p>
              </div>
              <label className="inline-field">
                <span>Tática</span>
                <select value={tactic} onChange={(event) => setTactic(event.target.value)}>
                  {tactics.map((option) => (
                    <option key={option} value={option}>{option}</option>
                  ))}
                </select>
              </label>
            </div>

            <div className="lineup-grid">
              {lineupPositions.map((position) => (
                <label key={position} className="lineup-row">
                  <span>{position}</span>
                  <select value={lineup[position] ?? ''} onChange={(event) => handleLineupChange(position, event.target.value)}>
                    <option value="">Selecione</option>
                    {selectedSquad.map((player) => (
                      <option key={player} value={player}>{player}</option>
                    ))}
                  </select>
                </label>
              ))}
            </div>
          </div>

          <div className="actions-row">
            <button onClick={confirmSquad}>Confirmar convocação</button>
            <button className="ghost" onClick={() => setScreen('menu')}>Voltar</button>
          </div>
          <p className="helper-text">{matchStatus}</p>
        </section>
      ) : null}

      {screen === 'match' ? (
        <main className="dashboard">
          <section className="card main-card">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Copa do Mundo</p>
                <h2>Brasil x {currentOpponent.name}</h2>
                <p className="subtitle">Você decide a escalação do Brasil. O adversário segue fixo.</p>
              </div>
              <div className="pill">Jogo {fixtureIndex + 1} de {opponents.length}</div>
            </div>

            <div className="scoreboard">
              <div className="team-box brazil-box">
                <span className="team-flag">🇧🇷</span>
                <strong>Brasil</strong>
                <span>{result.home}</span>
              </div>
              <div className="score-separator">:</div>
              <div className="team-box opponent-box">
                <span className="team-flag">{currentOpponent.flag}</span>
                <strong>{currentOpponent.name}</strong>
                <span>{result.away}</span>
              </div>
            </div>

            <div className="metrics">
              <article>
                <span>Posse</span>
                <strong>{result.possession.home}%</strong>
                <small>Brasil</small>
              </article>
              <article>
                <span>Finalizações</span>
                <strong>{result.shots.home}</strong>
                <small>Brasil</small>
              </article>
              <article>
                <span>Minuto</span>
                <strong>{result.minute}</strong>
                <small>da partida</small>
              </article>
            </div>

            <div className="live-status">
              <span className="live-dot" />
              <strong>{matchStatus}</strong>
            </div>

            <div className="timeline">
              <h3>Resumo</h3>
              <ul>
                {result.highlights.map((highlight) => (
                  <li key={highlight}>{highlight}</li>
                ))}
              </ul>
            </div>

            <div className="actions-row">
              <button onClick={simulateFixture}>{isMatchLive ? 'Pausar partida' : 'Iniciar partida'}</button>
              <button className="ghost" onClick={advanceFixture}>Próximo jogo</button>
              <button className="ghost" onClick={resetTournament}>Novo torneio</button>
            </div>
          </section>

          <aside className="side-panel">
            <section className="card">
              <div className="section-heading compact">
                <div>
                  <h3>Seu Brasil</h3>
                  <p className="subtitle">A única escalação editável</p>
                </div>
              </div>
              <div className="roster-list">
                {Object.entries(lineup).map(([position, player]) => (
                  <div key={position} className="roster-item">
                    <span>⚽</span>
                    <strong>{position}: {player || 'Sem jogador'}</strong>
                  </div>
                ))}
              </div>
            </section>

            <section className="card">
              <div className="section-heading compact">
                <div>
                  <h3>Status da escalação</h3>
                  <p className="subtitle">Acompanhe a stamina e faça substituições</p>
                </div>
              </div>
              <div className="status-list">
                {Object.entries(lineup).map(([position, player]) => {
                  const playerState = player ? playerStatuses[player] : null;
                  const statusClass = playerState?.stamina >= 85 ? 'status-good' : playerState?.stamina >= 70 ? 'status-warning' : 'status-tired';
                  return (
                    <div key={position} className="status-row">
                      <div className="status-player">
                        <strong>{position}</strong>
                        <span>{player || 'Sem jogador'}</span>
                        {player ? <small className={`status-badge ${statusClass}`}>{playerState?.status ?? 'Bom'} • {playerState?.stamina ?? 90}</small> : null}
                      </div>
                      {player && benchPlayers.length > 0 ? (
                        <div className="substitution-controls">
                          <select value={substitutionTargets[position] ?? ''} onChange={(event) => setSubstitutionTargets((currentTargets) => ({ ...currentTargets, [position]: event.target.value }))}>
                            <option value="">Substituir</option>
                            {benchPlayers.map((benchPlayer) => (
                              <option key={benchPlayer} value={benchPlayer}>{benchPlayer}</option>
                            ))}
                          </select>
                          <button className="ghost small-btn" onClick={() => handleSubstitution(position, substitutionTargets[position] ?? '')}>Entrar</button>
                        </div>
                      ) : null}
                    </div>
                  );
                })}
              </div>
            </section>

            <section className="card">
              <div className="section-heading compact">
                <div>
                  <h3>Adversário fixo</h3>
                  <p className="subtitle">{currentOpponent.formation}</p>
                </div>
              </div>
              <div className="roster-list">
                {currentOpponent.roster.map((player) => (
                  <div key={player} className="roster-item">
                    <span>{currentOpponent.flag}</span>
                    <strong>{player}</strong>
                  </div>
                ))}
              </div>
            </section>

            <section className="card">
              <div className="section-heading compact">
                <div>
                  <h3>Histórico</h3>
                  <p className="subtitle">Últimos confrontos simulados</p>
                </div>
              </div>
              <div className="history-list">
                {summary.length === 0 ? <p className="helper-text">Nenhum jogo simulado ainda.</p> : summary.map((entry) => (
                  <div key={`${entry.fixture}-${entry.result}`} className="history-item">
                    <strong>{entry.fixture}</strong>
                    <span>{entry.result}</span>
                  </div>
                ))}
              </div>
            </section>
          </aside>
        </main>
      ) : null}

      {screen === 'finished' ? (
        <section className="card panel-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Copa encerrada</p>
              <h2>Sua campanha no torneio foi simulada</h2>
              <p className="subtitle">Você pode reiniciar e testar outra convocação.</p>
            </div>
          </div>
          <div className="actions-row">
            <button onClick={resetTournament}>Voltar para o início</button>
          </div>
        </section>
      ) : null}
    </div>
  );
}

export default App;
