/**
 * Command Pattern for BoxOfficePOS
 * 
 * Mỗi thao tác tại quầy (thêm/bỏ ghế, thêm/bỏ bắp nước) được đóng gói thành
 * một Command object. PosCommandInvoker quản lý History Stack để cho phép Undo/Redo.
 */

// ── Command Interface ────────────────────────────────────────────────────────
class Command {
  execute() { throw new Error('execute() not implemented'); }
  undo()    { throw new Error('undo() not implemented');    }
  describe(){ return 'Command'; }
}

// ── Concrete Commands ────────────────────────────────────────────────────────

export class AddSeatCommand extends Command {
  constructor(seat, setSelectedSeats) {
    super();
    this.seat = seat;
    this.setSelectedSeats = setSelectedSeats;
  }
  execute() {
    this.setSelectedSeats(prev => {
      if (prev.find(s => s.seatId === this.seat.seatId)) return prev;
      if (prev.length >= 10) return prev;
      return [...prev, this.seat];
    });
  }
  undo() {
    this.setSelectedSeats(prev => prev.filter(s => s.seatId !== this.seat.seatId));
  }
  describe() { return `Thêm ghế ${this.seat.seatRow}${this.seat.seatNumber}`; }
}

export class RemoveSeatCommand extends Command {
  constructor(seat, setSelectedSeats) {
    super();
    this.seat = seat;
    this.setSelectedSeats = setSelectedSeats;
  }
  execute() {
    this.setSelectedSeats(prev => prev.filter(s => s.seatId !== this.seat.seatId));
  }
  undo() {
    this.setSelectedSeats(prev => {
      if (prev.find(s => s.seatId === this.seat.seatId)) return prev;
      return [...prev, this.seat];
    });
  }
  describe() { return `Bỏ ghế ${this.seat.seatRow}${this.seat.seatNumber}`; }
}

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
      return prev; // item was removed entirely - can't fully restore without storing snapshot
    });
  }
  describe() { return `Bỏ ${this.itemName}`; }
}

// ── Invoker (History Stack) ───────────────────────────────────────────────────
export class PosCommandInvoker {
  constructor() {
    this.history = [];   // executed commands
    this.redoStack = []; // undone commands available to redo
  }

  execute(command) {
    command.execute();
    this.history.push(command);
    this.redoStack = []; // clear redo on new action
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

  getHistoryLog() {
    return this.history.map((c, i) => ({ index: i + 1, action: c.describe() }));
  }

  reset() {
    this.history = [];
    this.redoStack = [];
  }
}
