import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";

const WS_URL = "ws://localhost:8080/ws";

/**
 * Observer Pattern — Frontend Hook (Concrete Observer).
 *
 * Custom hook đóng vai trò Observer, kết nối WebSocket STOMP (native, không SockJS)
 * và lắng nghe sự kiện thay đổi trạng thái ghế real-time từ Backend.
 *
 * Cách dùng trong component:
 * ```jsx
 * useSeatObserver(selectedShowtime?.showtimeId, (event) => {
 *   // event: { showtimeId, seatId, seatCode, status, triggeredByUserId }
 *   setSeats(prev => prev.map(s => s.seatId === event.seatId ? { ...s, status: event.status } : s));
 * });
 * ```
 *
 * @param {number|null} showtimeId   - ID suất chiếu cần theo dõi (null = không subscribe)
 * @param {function}    onSeatUpdate - Callback nhận SeatStatusEvent khi ghế thay đổi
 */
export function useSeatObserver(showtimeId, onSeatUpdate) {
  // Dùng ref để giữ callback mới nhất mà không trigger re-connect
  const onSeatUpdateRef = useRef(onSeatUpdate);
  useEffect(() => {
    onSeatUpdateRef.current = onSeatUpdate;
  }, [onSeatUpdate]);

  useEffect(() => {
    if (!showtimeId) return;

    const topic = `/topic/seats/${showtimeId}`;

    const client = new Client({
      brokerURL: WS_URL, // native WebSocket — không cần SockJS
      reconnectDelay: 5000,
      onConnect: () => {
        console.log(`[Observer] ✅ Kết nối WebSocket. Subscribe: ${topic}`);
        client.subscribe(topic, (message) => {
          try {
            const event = JSON.parse(message.body);
            console.log(`[Observer] 📡 Nhận: seat=${event.seatCode}, status=${event.status}`);
            onSeatUpdateRef.current?.(event);
          } catch (err) {
            console.warn("[Observer] Không parse được message:", err);
          }
        });
      },
      onDisconnect: () => {
        console.log(`[Observer] ⚠️ Mất kết nối WebSocket khỏi ${topic}`);
      },
      onStompError: (frame) => {
        console.error("[Observer] STOMP error:", frame.headers?.message);
      },
    });

    client.activate();

    return () => {
      console.log(`[Observer] 🔌 Hủy kết nối: ${topic}`);
      client.deactivate();
    };
  }, [showtimeId]);
}

