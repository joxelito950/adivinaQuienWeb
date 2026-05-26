import { cn } from '@/lib/utils'

interface TurnIndicatorProps {
  isMyTurn:      boolean
  isFinished?:   boolean
  didIWin?:      boolean
  opponentName?: string
  note?:         string | null
  className?:    string
}

export function TurnIndicator({
  isMyTurn,
  isFinished = false,
  didIWin = false,
  opponentName = 'Oponente',
  note,
  className,
}: TurnIndicatorProps) {
  const title = isFinished
    ? (didIWin ? '¡Ganaste la partida!' : 'Partida finalizada')
    : (isMyTurn ? '¡Es tu turno!' : `Turno de ${opponentName}`)

  const subtitle = isFinished
    ? (didIWin ? 'Adivinaste correctamente el personaje secreto.' : 'Tu oponente adivinó el personaje secreto.')
    : (isMyTurn ? 'Haz una pregunta o adivina el personaje secreto.' : 'Esperando la acción del oponente…')

  const dotClassName = isFinished
    ? (didIWin ? 'bg-emerald-400' : 'bg-amber-400')
    : (isMyTurn ? 'bg-brand-400' : 'bg-slate-500')

  return (
    <div
      className={cn(
        'flex items-center gap-3 rounded-xl border px-4 py-3 transition-all',
        isFinished
          ? didIWin
            ? 'border-emerald-500/70 bg-emerald-900/20 shadow-lg shadow-emerald-500/10'
            : 'border-amber-500/70 bg-amber-900/20 shadow-lg shadow-amber-500/10'
          : isMyTurn
            ? 'border-brand-500 bg-brand-900/30 shadow-lg shadow-brand-500/10'
            : 'border-slate-600 bg-slate-800/60',
        className,
      )}
    >
      {/* Indicador de pulso */}
      <span className="relative flex h-3 w-3 shrink-0">
        {isMyTurn && !isFinished && (
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-brand-400 opacity-75" />
        )}
        <span
          className={cn(
            'relative inline-flex h-3 w-3 rounded-full',
            dotClassName,
          )}
        />
      </span>

      <div className="min-w-0">
        <p
          className={cn(
            'text-sm font-semibold sm:text-base',
            isFinished
              ? didIWin
                ? 'text-emerald-300'
                : 'text-amber-300'
              : isMyTurn
                ? 'text-brand-300'
                : 'text-slate-300',
          )}
        >
          {title}
        </p>
        <p className="text-xs text-slate-500">
          {subtitle}
        </p>
        {note && (
          <p className="mt-2 rounded-lg border border-slate-600/80 bg-slate-900/60 px-3 py-2 text-xs text-slate-200 sm:text-sm">
            {note}
          </p>
        )}
      </div>
    </div>
  )
}
