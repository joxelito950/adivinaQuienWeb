'use client'

import Image from 'next/image'
import { KeyboardEvent, useState } from 'react'
import { CharacterCard as CharacterCardType } from '@/lib/protocol'
import { AttributeBadge } from '@/components/ui/Badge'
import { cn } from '@/lib/utils'

// Genera un color de avatar determinista a partir del nombre
const AVATAR_PALETTE = [
  'bg-rose-500', 'bg-orange-500', 'bg-amber-500', 'bg-lime-600',
  'bg-emerald-500', 'bg-cyan-500', 'bg-blue-500', 'bg-violet-500',
  'bg-pink-500', 'bg-teal-500', 'bg-indigo-500', 'bg-fuchsia-500',
]

function avatarColor(name: string): string {
  let hash = 0
  for (const ch of name) hash = (hash * 31 + ch.charCodeAt(0)) & 0xff
  return AVATAR_PALETTE[hash % AVATAR_PALETTE.length]
}

interface CharacterCardProps {
  character:    CharacterCardType
  isEliminated?: boolean
  isSecret?:     boolean
  isSelected?:   boolean
  onClick?:      () => void
  onZoomClick?:  () => void
  showGuessConfirm?: boolean
  onGuessConfirm?: () => void
}

export function CharacterCard({
  character,
  isEliminated = false,
  isSecret = false,
  isSelected = false,
  onClick,
  onZoomClick,
  showGuessConfirm = false,
  onGuessConfirm,
}: CharacterCardProps) {
  const [imageFailed, setImageFailed] = useState(false)
  const attributes = character.attributes ?? []
  const color   = avatarColor(character.displayName)
  const initial = character.displayName.charAt(0).toUpperCase()
  const imageUrl = character.imageUrl
  const shouldShowImage = Boolean(imageUrl) && !imageFailed
  const isInteractive = Boolean(onClick)

  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    if (!isInteractive || isEliminated) return
    if (event.key !== 'Enter' && event.key !== ' ') return
    event.preventDefault()
    onClick?.()
  }

  return (
    <div
      onClick={isInteractive && !isEliminated ? onClick : undefined}
      onKeyDown={handleKeyDown}
      role={isInteractive ? 'button' : undefined}
      tabIndex={isInteractive ? 0 : -1}
      aria-pressed={isInteractive ? isSelected : undefined}
      className={cn(
        'group relative flex min-h-[12.5rem] flex-col rounded-2xl border p-2 text-center',
        'min-w-0 overflow-hidden select-none transition-transform duration-200',
        'sm:min-h-[15rem]',
        isEliminated
          ? 'border-slate-700/50 bg-slate-800/30 opacity-40 grayscale'
          : isSelected
          ? 'border-brand-400 bg-brand-900/30 shadow-md shadow-brand-400/20 ring-1 ring-brand-400'
          : isInteractive
          ? 'cursor-pointer border-slate-700 bg-slate-800 hover:border-slate-500 hover:bg-slate-700 active:scale-[0.985]'
          : 'border-slate-700 bg-slate-800',
      )}
    >
      {onZoomClick && (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation()
            onZoomClick()
          }}
          className="absolute left-1.5 top-1.5 z-20 rounded-full border border-slate-500/70 bg-slate-900/80 px-1.5 py-0.5 text-xs text-slate-200 transition-colors hover:border-slate-300"
          aria-label={`Ampliar ${character.displayName}`}
        >
          🔍
        </button>
      )}

      {/* Avatar */}
      <div
        className={cn(
          'relative flex aspect-[4/5] w-full shrink-0 items-center justify-center overflow-hidden rounded-xl text-2xl font-bold text-white',
          'transition-transform duration-200 motion-reduce:transition-none',
          !isEliminated && 'group-hover:scale-105',
          !shouldShowImage && color,
        )}
      >
        {shouldShowImage ? (
          <Image
            src={imageUrl!}
            alt={character.displayName}
            fill
            sizes="(max-width: 640px) 40vw, (max-width: 1024px) 20vw, 14vw"
            className="object-contain p-1.5"
            onError={() => setImageFailed(true)}
          />
        ) : (
          initial
        )}
      </div>

      {/* Name */}
      <span className="mt-2 w-full truncate text-sm font-semibold text-slate-100 sm:text-base">
        {character.displayName}
      </span>

      {/* Attribute badges — solo en sm+ para no saturar mobile */}
      {!isEliminated && attributes.length > 0 && (
        <div className="mt-1 hidden flex-wrap justify-center gap-0.5 sm:flex">
          {attributes.map((attr) => (
            <AttributeBadge key={attr} attribute={attr} showLabel={false} />
          ))}
        </div>
      )}

      {showGuessConfirm && !isEliminated && (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation()
            onGuessConfirm?.()
          }}
          className="mt-2 rounded-lg border border-brand-300 bg-brand-500 px-2 py-1 text-xs font-semibold text-white transition-colors hover:bg-brand-600"
        >
          Confirmar adivinanza
        </button>
      )}

      {/* Indicador de personaje secreto del jugador */}
      {isSecret && (
        <span className="absolute right-1 top-1 rounded-full bg-brand-500 px-1 py-0.5 text-[9px] font-bold text-white leading-none">
          TÚ
        </span>
      )}

      {/* Overlay de personaje eliminado */}
      {isEliminated && (
        <div className="pointer-events-none absolute inset-0 flex items-center justify-center rounded-xl">
          <span className="rotate-[-15deg] text-xl opacity-80">❌</span>
        </div>
      )}
    </div>
  )
}
