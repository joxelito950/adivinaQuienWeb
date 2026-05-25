'use client'

import Image from 'next/image'
import { useState } from 'react'
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
}

export function CharacterCard({
  character,
  isEliminated = false,
  isSecret = false,
  isSelected = false,
  onClick,
}: CharacterCardProps) {
  const [imageFailed, setImageFailed] = useState(false)
  const attributes = character.attributes ?? []
  const color   = avatarColor(character.displayName)
  const initial = character.displayName.charAt(0).toUpperCase()
  const imageUrl = character.imageUrl
  const shouldShowImage = Boolean(imageUrl) && !imageFailed
  const Tag     = onClick ? 'button' : 'div'

  return (
    <Tag
      {...(onClick ? { onClick, type: 'button' as const, 'aria-pressed': isSelected } : {})}
      className={cn(
        'relative flex flex-col items-center gap-1 rounded-xl border p-2 text-center',
        'min-w-0 overflow-hidden transition-all select-none',
        isEliminated
          ? 'border-slate-700/50 bg-slate-800/30 opacity-40 grayscale'
          : isSelected
          ? 'border-brand-400 bg-brand-900/30 shadow-md shadow-brand-400/20 ring-1 ring-brand-400'
          : onClick
          ? 'border-slate-700 bg-slate-800 hover:border-slate-500 hover:bg-slate-700 cursor-pointer active:scale-95'
          : 'border-slate-700 bg-slate-800',
      )}
    >
      {/* Avatar */}
      <div
        className={cn(
          'relative flex h-9 w-9 shrink-0 items-center justify-center overflow-hidden rounded-full text-base font-bold text-white',
          'sm:h-12 sm:w-12 sm:text-lg',
          !shouldShowImage && color,
        )}
      >
        {shouldShowImage ? (
          <Image
            src={imageUrl!}
            alt={character.displayName}
            fill
            sizes="(max-width: 640px) 36px, 48px"
            className="object-cover"
            onError={() => setImageFailed(true)}
          />
        ) : (
          initial
        )}
      </div>

      {/* Name */}
      <span className="w-full truncate text-xs font-medium text-slate-200 sm:text-sm">
        {character.displayName}
      </span>

      {/* Attribute badges — solo en sm+ para no saturar mobile */}
      {!isEliminated && attributes.length > 0 && (
        <div className="hidden flex-wrap justify-center gap-0.5 sm:flex">
          {attributes.map((attr) => (
            <AttributeBadge key={attr} attribute={attr} showLabel={false} />
          ))}
        </div>
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
    </Tag>
  )
}
