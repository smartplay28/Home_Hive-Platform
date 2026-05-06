/**
 * useWebSocket — Core STOMP WebSocket hook.
 *
 * Manages a single persistent STOMP connection per component tree.
 * Handles:
 *   - Connection with SockJS fallback
 *   - Auto-reconnect on disconnect (built into @stomp/stompjs)
 *   - Token-based auth via STOMP headers
 *   - Graceful cleanup on unmount
 *
 * Usage:
 *   const { subscribe, isConnected } = useWebSocket();
 *   const unsub = subscribe('/topic/agents/123/new-order', (msg) => { ... });
 *   // unsub() on cleanup
 */

import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const envWsUrl = import.meta.env.VITE_WS_URL;
const WS_URL = envWsUrl ? envWsUrl.replace(/^ws:\/\//i, 'http://').replace(/^wss:\/\//i, 'https://') : 'http://localhost:8080';

export const useWebSocket = () => {
  const clientRef = useRef(null);
  const [isConnected, setIsConnected] = useState(false);
  const pendingSubscriptions = useRef([]);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');

    const client = new Client({
      // SockJS factory — provides fallback for non-WS environments
      webSocketFactory: () => new SockJS(`${WS_URL}/ws`),

      // Attach JWT token to STOMP connect headers
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},

      // Reconnect after 5s if disconnected
      reconnectDelay: 5000,

      onConnect: () => {
        setIsConnected(true);
        console.log('[WebSocket] Connected');

        // Flush any subscriptions requested before connection was ready
        pendingSubscriptions.current.forEach(({ destination, callback }) => {
          client.subscribe(destination, (frame) => {
            callback(JSON.parse(frame.body));
          });
        });
        pendingSubscriptions.current = [];
      },

      onDisconnect: () => {
        setIsConnected(false);
        console.log('[WebSocket] Disconnected');
      },

      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers['message']);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  /**
   * Subscribe to a STOMP topic/destination.
   * Returns an unsubscribe function.
   */
  const subscribe = useCallback((destination, callback) => {
    const client = clientRef.current;

    if (client?.connected) {
      const sub = client.subscribe(destination, (frame) => {
        callback(JSON.parse(frame.body));
      });
      return () => sub.unsubscribe();
    } else {
      // Queue subscription until connection is established
      pendingSubscriptions.current.push({ destination, callback });
      return () => {
        pendingSubscriptions.current = pendingSubscriptions.current
          .filter((s) => s.destination !== destination);
      };
    }
  }, []);

  return { subscribe, isConnected };
};
