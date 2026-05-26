import { QuestionKey } from '@/lib/protocol'
import { cn } from '@/lib/utils'

// ─── Attribute badge ──────────────────────────────────────────────────────────

const ATTRIBUTE_CONFIG: Record<QuestionKey, { emoji: string; label: string; color: string }> = {
  [QuestionKey.USES_GLASSES]:    { emoji: '👓', label: 'Lentes',       color: 'bg-sky-900/60    text-sky-300    border-sky-700'    },
  [QuestionKey.HAS_BEARD]:       { emoji: '🧔', label: 'Barba',        color: 'bg-amber-900/60  text-amber-300  border-amber-700'  },
  [QuestionKey.HAS_HAT]:         { emoji: '🎩', label: 'Sombrero',     color: 'bg-purple-900/60 text-purple-300 border-purple-700' },
  [QuestionKey.HAS_BLONDE_HAIR]: { emoji: '👱', label: 'Pelo rubio',   color: 'bg-yellow-900/60 text-yellow-300 border-yellow-700' },
  [QuestionKey.HAS_LONG_HAIR]:   { emoji: '💇', label: 'Cabello largo', color: 'bg-fuchsia-900/60 text-fuchsia-300 border-fuchsia-700' },
  [QuestionKey.HAS_SHORT_HAIR]:  { emoji: '✂️', label: 'Cabello corto', color: 'bg-cyan-900/60 text-cyan-300 border-cyan-700' },
  [QuestionKey.HAS_STRAIGHT_HAIR]: { emoji: '🪮', label: 'Cabello liso', color: 'bg-teal-900/60 text-teal-300 border-teal-700' },
  [QuestionKey.HAS_CURLY_HAIR]:  { emoji: '🌀', label: 'Cabello crespo', color: 'bg-emerald-900/60 text-emerald-300 border-emerald-700' },
  [QuestionKey.HAS_EARRINGS]:    { emoji: '💎', label: 'Aretes',       color: 'bg-pink-900/60   text-pink-300   border-pink-700'   },
  [QuestionKey.IS_MALE]:         { emoji: '♂',  label: 'Hombre',       color: 'bg-cyan-900/60   text-cyan-300   border-cyan-700'   },
  [QuestionKey.IS_FEMALE]:       { emoji: '♀',  label: 'Mujer',        color: 'bg-rose-900/60   text-rose-300   border-rose-700'   },
  [QuestionKey.IS_BALD]:         { emoji: '🪒', label: 'Calvo',        color: 'bg-stone-900/60  text-stone-300  border-stone-700'  },
  [QuestionKey.HAS_FAIR_SKIN]:   { emoji: '☀',  label: 'Tez clara',    color: 'bg-orange-900/60 text-orange-300 border-orange-700' },
  [QuestionKey.HAS_DARK_SKIN]:   { emoji: '🌙', label: 'Tez oscura',   color: 'bg-indigo-900/60 text-indigo-300 border-indigo-700' },
}

interface AttributeBadgeProps {
  attribute: QuestionKey
  showLabel?: boolean
  className?: string
}

export function AttributeBadge({ attribute, showLabel = true, className }: AttributeBadgeProps) {
  const { emoji, label, color } = ATTRIBUTE_CONFIG[attribute]
  return (
    <span
      className={cn(
        'inline-flex items-center gap-0.5 rounded-full border px-1.5 py-0.5 text-xs font-medium',
        color,
        className,
      )}
      title={label}
    >
      <span aria-hidden="true">{emoji}</span>
      {showLabel && <span className="hidden sm:inline">{label}</span>}
    </span>
  )
}

// ─── Generic badge ────────────────────────────────────────────────────────────

type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'info'

const genericVariants: Record<BadgeVariant, string> = {
  default: 'bg-slate-700  text-slate-300  border-slate-600',
  success: 'bg-green-900/60  text-green-300  border-green-700',
  warning: 'bg-yellow-900/60 text-yellow-300 border-yellow-700',
  danger:  'bg-red-900/60    text-red-300    border-red-700',
  info:    'bg-sky-900/60    text-sky-300    border-sky-700',
}

interface BadgeProps {
  children:  React.ReactNode
  variant?:  BadgeVariant
  className?: string
}

export function Badge({ children, variant = 'default', className }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold',
        genericVariants[variant],
        className,
      )}
    >
      {children}
    </span>
  )
}
