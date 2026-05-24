import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createWsClient } from '@/lib/ws-client'

class MockWebSocket {
  static OPEN = 1
  static CLOSED = 3

  readyState = MockWebSocket.OPEN
  sent: string[] = []
  listeners: Record<string, Array<(...args: any[]) => void>> = {}

  constructor(public url: string) {}

  addEventListener(type: string, listener: (...args: any[]) => void): void {
    this.listeners[type] ??= []
    this.listeners[type].push(listener)
  }

  send(data: string): void {
    this.sent.push(data)
  }

  close(): void {
    this.readyState = MockWebSocket.CLOSED
  }

  emit(type: string, eventData: any = {}): void {
    for (const listener of this.listeners[type] ?? []) {
      listener(eventData)
    }
  }
}

describe('ws client', () => {
  beforeEach(() => {
    vi.unstubAllGlobals()
    vi.stubGlobal('WebSocket', MockWebSocket as any)
  })

  it('creates a socket with explicit URL and sends serialized messages', () => {
    const onOpen = vi.fn()
    const client = createWsClient({ onOpen }, 'ws://localhost:9999/ws')

    expect(client).not.toBeNull()
    const socket = client!.socket as unknown as MockWebSocket
    expect(socket.url).toBe('ws://localhost:9999/ws')

    socket.emit('open')
    expect(onOpen).toHaveBeenCalledOnce()

    client!.send({
      type: 'ping',
      payload: {},
    })

    expect(socket.sent).toHaveLength(1)
    expect(socket.sent[0]).toContain('"type":"ping"')
  })

  it('does not send when socket is not open', () => {
    const client = createWsClient({}, 'ws://localhost:9999/ws')
    const socket = client!.socket as unknown as MockWebSocket

    socket.readyState = 0
    client!.send({ type: 'ping', payload: {} })

    expect(socket.sent).toHaveLength(0)
  })

  it('returns null when WebSocket is unavailable', () => {
    vi.unstubAllGlobals()
    vi.stubGlobal('WebSocket', undefined)

    const client = createWsClient({}, 'ws://localhost:9999/ws')
    expect(client).toBeNull()
  })
})
