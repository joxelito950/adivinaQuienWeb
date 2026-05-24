'use client'

import { Board, Difficulty, DifficultyWire } from '@/lib/protocol'
import { CharacterCard } from './CharacterCard'
import { cn } from '@/lib/utils'

/**
 * Columnas de la grilla por dificultad y breakpoint.
 *   small  (3×4):  3 cols por defecto → 4 cols en sm+
 *   medium (4×5):  3 cols por defecto → 4 cols en sm+ → 5 cols en md+
 *   large  (6×6):  3 cols por defecto → 4 cols en sm+ → 5 cols en md+ → 6 cols en lg+
 */
const GRID_COLS: Record<Difficulty, string> = {
  [Difficulty.SMALL]:  'grid-cols-3 sm:grid-cols-4',
  [Difficulty.MEDIUM]: 'grid-cols-3 sm:grid-cols-4 md:grid-cols-5',
  [Difficulty.LARGE]:  'grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6',
}

interface BoardGridProps {
  board:              Board
  difficulty:         Difficulty | DifficultyWire
  /** IDs de personajes eliminados (ya no visibles) */
  eliminatedIds?:     string[]
  /** ID del personaje secreto del jugador local */
  secretCharacterId?: string
  /** Activa el modo selección (para la acción de adivinar) */
  selectable?:        boolean
  selectedId?:        string
  onSelect?:          (characterId: string) => void
}

export function BoardGrid({
  board,
  difficulty,
  eliminatedIds = [],
  secretCharacterId,
  selectable = false,
  selectedId,
  onSelect,
}: BoardGridProps) {
  const eliminatedSet = new Set(eliminatedIds)
  const normalizedDifficulty = String(difficulty).toUpperCase() as Difficulty

  return (
    <div className={cn('grid gap-2 sm:gap-3', GRID_COLS[normalizedDifficulty] ?? GRID_COLS[Difficulty.SMALL])}>
      {board.characters.map((character) => {
        const isEliminated = eliminatedSet.has(character.characterId)
        const handleClick =
          selectable && !isEliminated
            ? () => onSelect?.(character.characterId)
            : undefined

        return (
          <CharacterCard
            key={character.characterId}
            character={character}
            isEliminated={isEliminated}
            isSecret={character.characterId === secretCharacterId}
            isSelected={character.characterId === selectedId}
            onClick={handleClick}
          />
        )
      })}
    </div>
  )
}
