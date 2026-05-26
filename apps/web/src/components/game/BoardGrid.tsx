'use client'

import Image from 'next/image'
import { useState } from 'react'
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
  onConfirmSelected?: (characterId: string) => void
}

export function BoardGrid({
  board,
  difficulty,
  eliminatedIds = [],
  secretCharacterId,
  selectable = false,
  selectedId,
  onSelect,
  onConfirmSelected,
}: BoardGridProps) {
  const [zoomCharacterId, setZoomCharacterId] = useState<string | null>(null)
  const eliminatedSet = new Set(eliminatedIds)
  const normalizedDifficulty = String(difficulty).toUpperCase() as Difficulty
  const zoomCharacter = board.characters.find((character) => character.characterId === zoomCharacterId) ?? null

  return (
    <>
      <div className={cn('grid gap-2.5 sm:gap-3.5', GRID_COLS[normalizedDifficulty] ?? GRID_COLS[Difficulty.SMALL])}>
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
              onZoomClick={() => setZoomCharacterId(character.characterId)}
              showGuessConfirm={selectable && character.characterId === selectedId}
              onGuessConfirm={() => onConfirmSelected?.(character.characterId)}
            />
          )
        })}
      </div>

      {zoomCharacter && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/85 p-4"
          role="dialog"
          aria-modal="true"
          aria-label={`Vista ampliada de ${zoomCharacter.displayName}`}
        >
          <button
            type="button"
            className="absolute right-4 top-4 rounded-lg border border-slate-500 bg-slate-900/80 px-3 py-1 text-sm text-slate-200"
            onClick={() => setZoomCharacterId(null)}
          >
            Cerrar
          </button>
          <div className="w-full max-w-sm rounded-2xl border border-slate-700 bg-slate-900 p-4 shadow-2xl">
            <div className="relative mx-auto aspect-[4/5] w-full overflow-hidden rounded-xl bg-slate-800">
              {zoomCharacter.imageUrl ? (
                <Image
                  src={zoomCharacter.imageUrl}
                  alt={zoomCharacter.displayName}
                  fill
                  sizes="(max-width: 768px) 80vw, 420px"
                  className="object-contain p-2"
                />
              ) : (
                <div className="flex h-full items-center justify-center text-4xl font-bold text-slate-200">
                  {zoomCharacter.displayName.charAt(0).toUpperCase()}
                </div>
              )}
            </div>
            <p className="mt-3 text-center text-lg font-semibold text-slate-100">{zoomCharacter.displayName}</p>
          </div>
        </div>
      )}
    </>
  )
}
