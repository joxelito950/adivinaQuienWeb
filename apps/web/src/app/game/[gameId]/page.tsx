import { Difficulty, QuestionKey } from '@/lib/protocol'
import type { Board, CharacterCard as CharacterCardType } from '@/lib/protocol'
import { BoardGrid }      from '@/components/game/BoardGrid'
import { TurnIndicator }  from '@/components/game/TurnIndicator'
import { ActionPanel }    from '@/components/game/ActionPanel'
import { GameLog }        from '@/components/game/GameLog'
import type { LogEntry }  from '@/components/game/GameLog'
import { Badge }          from '@/components/ui/Badge'

// ─── Datos stub para visualización del skeleton ───────────────────────────────

const STUB_CHARACTERS: CharacterCardType[] = [
  { characterId:  '1', displayName: 'Ana',     attributes: [QuestionKey.HAS_BLONDE_HAIR, QuestionKey.HAS_BLUE_EYES] },
  { characterId:  '2', displayName: 'Bruno',   attributes: [QuestionKey.HAS_BEARD] },
  { characterId:  '3', displayName: 'Carmen',  attributes: [QuestionKey.USES_GLASSES, QuestionKey.HAS_EARRINGS] },
  { characterId:  '4', displayName: 'Diego',   attributes: [QuestionKey.HAS_HAT, QuestionKey.HAS_BEARD] },
  { characterId:  '5', displayName: 'Elena',   attributes: [QuestionKey.HAS_BLONDE_HAIR, QuestionKey.HAS_EARRINGS] },
  { characterId:  '6', displayName: 'Felipe',  attributes: [QuestionKey.USES_GLASSES] },
  { characterId:  '7', displayName: 'Gloria',  attributes: [QuestionKey.HAS_BLUE_EYES, QuestionKey.HAS_EARRINGS] },
  { characterId:  '8', displayName: 'Héctor',  attributes: [QuestionKey.HAS_BEARD, QuestionKey.HAS_HAT] },
  { characterId:  '9', displayName: 'Irene',   attributes: [QuestionKey.HAS_BLONDE_HAIR] },
  { characterId: '10', displayName: 'Javier',  attributes: [QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD] },
  { characterId: '11', displayName: 'Karen',   attributes: [QuestionKey.HAS_BLUE_EYES] },
  { characterId: '12', displayName: 'Luis',    attributes: [QuestionKey.HAS_HAT] },
]

const STUB_BOARD: Board = { rows: 3, cols: 4, characters: STUB_CHARACTERS }

const STUB_LOG: LogEntry[] = [
  { type: 'event',    message: 'La partida ha comenzado.' },
  { type: 'question', by: 'Oponente', key: QuestionKey.HAS_BEARD,       answer: true  },
  { type: 'question', by: 'Tú',       key: QuestionKey.HAS_BLONDE_HAIR, answer: false },
]

// ─── Page ─────────────────────────────────────────────────────────────────────

interface PageProps {
  params: Promise<{ gameId: string }>
}

export default async function GamePage({ params }: PageProps) {
  const { gameId } = await params

  // Stub: en producción este estado vendrá del hook WebSocket
  const isMyTurn          = true
  const secretCharacterId = '5'
  const eliminatedIds     = ['2', '8']

  return (
    <main className="py-6 sm:py-8 lg:py-10">
      {/* ── Header ─────────────────────────────────────────────────────── */}
      <header className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-100 sm:text-2xl">Adivina Quién</h1>
          <p className="text-xs text-slate-500">
            Partida:{' '}
            <span className="font-mono text-slate-400">{gameId}</span>
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge variant="info">Dificultad: Pequeño</Badge>
          <Badge variant="success">En progreso</Badge>
        </div>
      </header>

      {/* ── Turn indicator ─────────────────────────────────────────────── */}
      <TurnIndicator
        isMyTurn={isMyTurn}
        opponentName="Oponente"
        className="mb-6"
      />

      {/*
       * ── Layout principal ──────────────────────────────────────────────
       * Mobile:   columna única → tablero arriba, panel lateral abajo
       * lg+:      dos columnas  → tablero | panel lateral fijo
       */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px] lg:items-start">

        {/* Tablero */}
        <section>
          <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Tu tablero
          </h2>
          <BoardGrid
            board={STUB_BOARD}
            difficulty={Difficulty.SMALL}
            secretCharacterId={secretCharacterId}
            eliminatedIds={eliminatedIds}
          />
        </section>

        {/* Panel lateral: acción + historial */}
        <aside className="flex flex-col gap-4 lg:sticky lg:top-6">
          <ActionPanel
            isMyTurn={isMyTurn}
            opponentCharacterOptions={STUB_CHARACTERS.map(({ characterId, displayName }) => ({
              characterId,
              displayName,
            }))}
          />
          <GameLog entries={STUB_LOG} />
        </aside>
      </div>
    </main>
  )
}
