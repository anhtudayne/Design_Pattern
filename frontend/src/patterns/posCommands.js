import { BASE_URL, getAuthHeaders } from '../utils/api';

/**
 * Command Pattern for BoxOfficePOS
 * 
 * Mỗi thao tác tại quầy được đóng gói thành một Command object.
 * Sự kết hợp: Command xử lý logic Local, sau đó trigger Observer (qua API) để đồng bộ Global.
 */

// ── Command Interface ────────────────────────────────────────────────────────
class Command {
  execute() { throw new Error('execute() not implemented'); }
  undo()    { throw new Error('undo() not implemented');    }
  describe(){ return 'Command'; }
}

const logApiError = (msg) => console.error(`[Command Sync] ${msg}`);

// Helper gọi API tách biệt khỏi logic State của React để tránh side-effect
const apiLock = (sid, seatId, uid) => 
  fetch(`${BASE_URL}/booking/lock?showtimeId=${sid}&seatId=${seatId}&userId=${uid}`, {
    method: 'POST', headers: getAuthHeaders()
  }).catch(err => logApiError(`Lock failed: ${err.message}`));

const apiUnlock = (sid, seatId) => 
  fetch(`${BASE_URL}/booking/unlock?showtimeId=${sid}&seatId=${seatId}`, {
    method: 'POST', headers: getAuthHeaders()
  }).catch(err => logApiError(`Unlock failed: ${err.message}`));

// ── Concrete Commands ────────────────────────────────────────────────────────

export class AddSeatCommand extends Command {
  constructor(seat, showtimeId, userId, setSelectedSeats) {
    super();
    this.seat = seat;
    this.showtimeId = showtimeId;
    this.userId = userId;
    this.setSelectedSeats = setSelectedSeats;
  }
  execute() {
    // 1. Cập nhật UI ngay lập tức (Optimistic UI)
    this.setSelectedSeats(prev => {
      if (prev.find(s => s.seatId === this.seat.seatId)) return prev;
      return [...prev, this.seat];
    });
    // 2. Trigger Observer thông qua Backend API (Side-effect đặt bên ngoài)
    apiLock(this.showtimeId, this.seat.seatId, this.userId);
  }
  undo() {
    this.setSelectedSeats(prev => prev.filter(s => s.seatId !== this.seat.seatId));
    apiUnlock(this.showtimeId, this.seat.seatId);
  }
  describe() { return `Thêm ghế ${this.seat.seatRow}${this.seat.seatNumber}`; }
}

export class RemoveSeatCommand extends Command {
  constructor(seat, showtimeId, userId, setSelectedSeats) {
    super();
    this.seat = seat;
    this.showtimeId = showtimeId;
    this.userId = userId;
    this.setSelectedSeats = setSelectedSeats;
  }
  execute() {
    this.setSelectedSeats(prev => prev.filter(s => s.seatId !== this.seat.seatId));
    apiUnlock(this.showtimeId, this.seat.seatId);
  }
  undo() {
    this.setSelectedSeats(prev => {
      if (prev.find(s => s.seatId === this.seat.seatId)) return prev;
      return [...prev, this.seat];
    });
    apiLock(this.showtimeId, this.seat.seatId, this.userId);
  }
  describe() { return `Bỏ ghế ${this.seat.seatRow}${this.seat.seatNumber}`; }
}

// ... các Command khác (AddFnbCommand, RemoveFnbCommand) giữ nguyên logic ...

export class AddFnbCommand extends Command {
  constructor(item, setCartFnb) {
    super();
    this.item = item;
    this.setCartFnb = setCartFnb;
  }
  execute() {
    this.setCartFnb(prev => {
      const exists = prev.find(f => f.itemId === this.item.itemId);
      if (exists) return prev.map(f => f.itemId === this.item.itemId ? { ...f, quantity: f.quantity + 1 } : f);
      return [...prev, { ...this.item, quantity: 1 }];
    });
  }
  undo() {
    this.setCartFnb(prev => {
      const item = prev.find(f => f.itemId === this.item.itemId);
      if (!item) return prev;
      if (item.quantity <= 1) return prev.filter(f => f.itemId !== this.item.itemId);
      return prev.map(f => f.itemId === this.item.itemId ? { ...f, quantity: f.quantity - 1 } : f);
    });
  }
  describe() { return `Thêm ${this.item.name}`; }
}

export class RemoveFnbCommand extends Command {
  constructor(itemId, itemName, setCartFnb) {
    super();
    this.itemId = itemId;
    this.itemName = itemName;
    this.setCartFnb = setCartFnb;
  }
  execute() {
    this.setCartFnb(prev => {
      const item = prev.find(f => f.itemId === this.itemId);
      if (!item) return prev;
      if (item.quantity <= 1) return prev.filter(f => f.itemId !== this.itemId);
      return prev.map(f => f.itemId === this.itemId ? { ...f, quantity: f.quantity - 1 } : f);
    });
  }
  undo() {
    this.setCartFnb(prev => {
      const exists = prev.find(f => f.itemId === this.itemId);
      if (exists) return prev.map(f => f.itemId === this.itemId ? { ...f, quantity: f.quantity + 1 } : f);
      return prev;
    });
  }
  describe() { return `Bỏ ${this.itemName}`; }
}

export class PosCommandInvoker {
  constructor() {
    this.history = [];
    this.redoStack = [];
  }
  execute(command) {
    command.execute();
    this.history.push(command);
    this.redoStack = [];
    return command.describe();
  }
  canUndo() { return this.history.length > 0; }
  canRedo() { return this.redoStack.length > 0; }
  undo() {
    if (!this.canUndo()) return null;
    const command = this.history.pop();
    command.undo();
    this.redoStack.push(command);
    return `Đã hoàn tác: ${command.describe()}`;
  }
  redo() {
    if (!this.canRedo()) return null;
    const command = this.redoStack.pop();
    command.execute();
    this.history.push(command);
    return `Đã thực hiện lại: ${command.describe()}`;
  }
  reset() { this.history = []; this.redoStack = []; }
}
