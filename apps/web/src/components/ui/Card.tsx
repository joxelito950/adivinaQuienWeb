import { cn } from '@/lib/utils'

interface CardProps {
  children:   React.ReactNode
  className?: string
  as?:        React.ElementType
}

export function Card({ children, className, as: Tag = 'div' }: CardProps) {
  return (
    <Tag
      className={cn(
        'rounded-2xl border border-slate-700 bg-slate-800/60 p-4 shadow-lg backdrop-blur-sm',
        className,
      )}
    >
      {children}
    </Tag>
  )
}
