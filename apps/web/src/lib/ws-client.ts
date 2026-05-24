import { ClientMessage } from '@/lib/protocol'

export interface WsClient {
  socket: WebSocket
  send: (message: ClientMessage) => void
  close: () => void
}

export interface WsHandlers {
  onOpen?: () => void
  onMessage?: (rawData: string) => void
  onClose?: () => void
  onError?: (event: Event) => void
}

export function createWsClient(handlers: WsHandlers = {}): WsClient | null {
  if (typeof window === 'undefined') return null

  const wsUrl = process.env.NEXT_PUBLIC_WS_URL ?? 'ws://localhost:8080/ws'
  const socket = new WebSocket(wsUrl)

  if (handlers.onOpen) {
    socket.addEventListener('open', handlers.onOpen)
  }

  if (handlers.onMessage) {
    socket.addEventListener('message', (event) => {
      handlers.onMessage?.(String(event.data ?? ''))
    })
  }

  if (handlers.onClose) {
    socket.addEventListener('close', handlers.onClose)
  }

  if (handlers.onError) {
    socket.addEventListener('error', handlers.onError)
  }

  return {
    socket,
    send(message) {
      if (socket.readyState !== WebSocket.OPEN) return
      socket.send(JSON.stringify(message))
    },
    close() {
      if (socket.readyState === WebSocket.CLOSED) return
      socket.close()
    },
  }
}
