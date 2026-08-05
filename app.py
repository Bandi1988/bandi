import json
import os
import tkinter as tk
from tkinter import ttk, messagebox
from dataclasses import dataclass, asdict

APP_TITLE = "Bandi Production Suite"
DATA_FILE = "app_data.json"
SHIFTS = ["A1", "A2", "B1", "B2", "C1", "C2", "JIS A", "JIS B", "JIS C"]
SECTIONS = ["Pozycje", "Szkolenia", "OJT", "Navodki", "Rework", "Odkaz", "Ukoly", "Reporty"]


@dataclass
class Operator:
    first_name: str
    last_name: str
    operator_id: str
    shift: str


class DataStore:
    def __init__(self, path: str):
        self.path = path
        self.data = {"operators": []}
        self.load()

    def load(self):
        if not os.path.exists(self.path):
            self.save()
            return
        try:
            with open(self.path, "r", encoding="utf-8") as f:
                self.data = json.load(f)
            self.data.setdefault("operators", [])
        except (json.JSONDecodeError, OSError):
            self.data = {"operators": []}
            self.save()

    def save(self):
        with open(self.path, "w", encoding="utf-8") as f:
            json.dump(self.data, f, ensure_ascii=False, indent=2)

    def add_operator(self, op: Operator):
        self.data["operators"].append(asdict(op))
        self.save()

    def get_operators(self, shift_filter: str = "Wszyscy"):
        all_ops = self.data.get("operators", [])
        if shift_filter == "Wszyscy":
            return all_ops
        return [op for op in all_ops if op.get("shift") == shift_filter]


class BandiApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(APP_TITLE)
        self.geometry("1200x760")
        self.minsize(1000, 680)
        self.configure(bg="#0E111B")

        self.store = DataStore(DATA_FILE)
        self.current_shift_filter = "Wszyscy"

        self.style = ttk.Style(self)
        self._setup_style()

        self.root_container = tk.Frame(self, bg="#0E111B")
        self.root_container.pack(fill="both", expand=True)

        self.render_main_dashboard()

    def _setup_style(self):
        self.style.theme_use("clam")
        self.style.configure("Card.TFrame", background="#171B2A")
        self.style.configure("Main.TLabel", background="#0E111B", foreground="#F8FAFF", font=("Segoe UI", 26, "bold"))
        self.style.configure("Subtitle.TLabel", background="#0E111B", foreground="#BAC5DD", font=("Segoe UI", 12))
        self.style.configure("Section.TButton", font=("Segoe UI", 13, "bold"), foreground="#F1F5FF", background="#2E3F74", padding=18)
        self.style.map("Section.TButton", background=[("active", "#4D63A9")])
        self.style.configure("Action.TButton", font=("Segoe UI", 11, "bold"), foreground="#F1F5FF", background="#305CA8", padding=10)
        self.style.map("Action.TButton", background=[("active", "#4C7FD8")])

    def clear_container(self):
        for child in self.root_container.winfo_children():
            child.destroy()

    def render_main_dashboard(self):
        self.clear_container()

        header = ttk.Label(self.root_container, text="MEGA DESIGN - PANEL GŁÓWNY", style="Main.TLabel")
        header.pack(pady=(30, 8))

        subtitle = ttk.Label(self.root_container, text="Wybierz dział, aby kontynuować pracę", style="Subtitle.TLabel")
        subtitle.pack(pady=(0, 24))

        grid = tk.Frame(self.root_container, bg="#0E111B")
        grid.pack(fill="both", expand=True, padx=50, pady=20)

        for i in range(2):
            grid.grid_rowconfigure(i, weight=1)
        for j in range(4):
            grid.grid_columnconfigure(j, weight=1)

        for idx, section in enumerate(SECTIONS):
            row, col = divmod(idx, 4)
            action = self.render_positions_view if section == "Pozycje" else self.show_coming_soon
            btn = ttk.Button(grid, text=section, style="Section.TButton", command=lambda s=section, a=action: a(s))
            btn.grid(row=row, column=col, padx=18, pady=18, sticky="nsew")

    def show_coming_soon(self, section_name: str):
        messagebox.showinfo("W przygotowaniu", f"Dział '{section_name}' będzie dostępny w kolejnych etapach.")

    def render_positions_view(self, _=None):
        self.clear_container()

        top = tk.Frame(self.root_container, bg="#0E111B")
        top.pack(fill="x", padx=24, pady=18)

        ttk.Button(top, text="← Powrót", style="Action.TButton", command=self.render_main_dashboard).pack(side="left")
        ttk.Label(top, text="Pozycje", style="Main.TLabel").pack(side="left", padx=24)

        cards = tk.Frame(self.root_container, bg="#0E111B")
        cards.pack(expand=True)

        operators_card = tk.Frame(cards, bg="#171B2A", width=360, height=260, highlightbackground="#3B4A71", highlightthickness=2)
        operators_card.pack(side="left", padx=28, pady=50)
        operators_card.pack_propagate(False)

        tk.Label(operators_card, text="Operatorzy", bg="#171B2A", fg="#F8FAFF", font=("Segoe UI", 23, "bold")).pack(pady=(55, 18))
        ttk.Button(operators_card, text="Otwórz", style="Action.TButton", command=self.render_operators_view).pack()

        positions_card = tk.Frame(cards, bg="#171B2A", width=360, height=260, highlightbackground="#3B4A71", highlightthickness=2)
        positions_card.pack(side="left", padx=28, pady=50)
        positions_card.pack_propagate(False)

        tk.Label(positions_card, text="Pozycje", bg="#171B2A", fg="#F8FAFF", font=("Segoe UI", 23, "bold")).pack(pady=(55, 18))
        ttk.Button(positions_card, text="W przygotowaniu", style="Action.TButton", command=lambda: self.show_coming_soon("Pozycje - widok szczegółowy")).pack()

    def render_operators_view(self):
        self.clear_container()

        top = tk.Frame(self.root_container, bg="#0E111B")
        top.pack(fill="x", padx=24, pady=18)

        ttk.Button(top, text="← Powrót do Pozycje", style="Action.TButton", command=self.render_positions_view).pack(side="left")
        ttk.Label(top, text="Operatorzy", style="Main.TLabel").pack(side="left", padx=20)
        ttk.Button(top, text="+ Dodaj operatora", style="Action.TButton", command=self.open_add_operator_dialog).pack(side="right")

        body = tk.Frame(self.root_container, bg="#0E111B")
        body.pack(fill="both", expand=True, padx=24, pady=(0, 18))

        filters = tk.Frame(body, bg="#131829", width=220)
        filters.pack(side="left", fill="y", padx=(0, 16))
        filters.pack_propagate(False)

        tk.Label(filters, text="Zmiany", bg="#131829", fg="#F8FAFF", font=("Segoe UI", 14, "bold")).pack(anchor="w", padx=16, pady=(18, 10))

        self._add_filter_button(filters, "Wszyscy")
        for shift in SHIFTS:
            self._add_filter_button(filters, shift)

        list_container = tk.Frame(body, bg="#0E111B")
        list_container.pack(side="left", fill="both", expand=True)

        self.operators_canvas = tk.Canvas(list_container, bg="#0E111B", highlightthickness=0)
        scrollbar = ttk.Scrollbar(list_container, orient="vertical", command=self.operators_canvas.yview)
        self.operators_scroll_frame = tk.Frame(self.operators_canvas, bg="#0E111B")

        self.operators_scroll_frame.bind(
            "<Configure>",
            lambda e: self.operators_canvas.configure(scrollregion=self.operators_canvas.bbox("all"))
        )

        self.operators_canvas.create_window((0, 0), window=self.operators_scroll_frame, anchor="nw")
        self.operators_canvas.configure(yscrollcommand=scrollbar.set)

        self.operators_canvas.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

        self.refresh_operators_list()

    def _add_filter_button(self, parent, label):
        button_bg = "#2A3D75" if label == self.current_shift_filter else "#1E294A"
        btn = tk.Button(
            parent,
            text=label,
            bg=button_bg,
            fg="#F4F7FF",
            relief="flat",
            activebackground="#3A5299",
            activeforeground="#FFFFFF",
            font=("Segoe UI", 10, "bold"),
            command=lambda l=label: self.change_filter(l),
            pady=7,
        )
        btn.pack(fill="x", padx=12, pady=4)

    def change_filter(self, label):
        self.current_shift_filter = label
        self.render_operators_view()

    def refresh_operators_list(self):
        for widget in self.operators_scroll_frame.winfo_children():
            widget.destroy()

        operators = self.store.get_operators(self.current_shift_filter)

        if not operators:
            tk.Label(
                self.operators_scroll_frame,
                text="Brak operatorów dla tego filtra.",
                bg="#0E111B",
                fg="#A8B2CA",
                font=("Segoe UI", 14)
            ).pack(pady=40)
            return

        for op in operators:
            card = tk.Frame(self.operators_scroll_frame, bg="#171B2A", highlightbackground="#2D3D66", highlightthickness=1)
            card.pack(fill="x", padx=8, pady=8)

            full_name = f"{op.get('first_name', '')} {op.get('last_name', '')}".strip()
            tk.Label(card, text=full_name, bg="#171B2A", fg="#F8FAFF", font=("Segoe UI", 14, "bold")).pack(anchor="w", padx=14, pady=(12, 4))
            tk.Label(card, text=f"ID: {op.get('operator_id', '-')}", bg="#171B2A", fg="#D1DAF2", font=("Segoe UI", 11)).pack(anchor="w", padx=14)
            tk.Label(card, text=f"Zmiana: {op.get('shift', '-')}", bg="#171B2A", fg="#93B4FF", font=("Segoe UI", 11, "bold")).pack(anchor="w", padx=14, pady=(0, 12))

    def open_add_operator_dialog(self):
        dialog = tk.Toplevel(self)
        dialog.title("Nowy operator")
        dialog.geometry("420x340")
        dialog.configure(bg="#131829")
        dialog.transient(self)
        dialog.grab_set()

        fields = {}

        def add_input(label_text, row, combobox_values=None):
            tk.Label(dialog, text=label_text, bg="#131829", fg="#E6ECFF", font=("Segoe UI", 11, "bold")).grid(row=row, column=0, padx=16, pady=10, sticky="w")
            if combobox_values:
                widget = ttk.Combobox(dialog, values=combobox_values, state="readonly")
                widget.current(0)
            else:
                widget = tk.Entry(dialog, bg="#1F2740", fg="#F8FAFF", insertbackground="#F8FAFF", relief="flat", font=("Segoe UI", 11))
            widget.grid(row=row, column=1, padx=16, pady=10, sticky="ew")
            fields[label_text] = widget

        dialog.grid_columnconfigure(1, weight=1)

        add_input("Imię", 0)
        add_input("Nazwisko", 1)
        add_input("ID", 2)
        add_input("Zmiana", 3, SHIFTS)

        def save_operator():
            first_name = fields["Imię"].get().strip()
            last_name = fields["Nazwisko"].get().strip()
            op_id = fields["ID"].get().strip()
            shift = fields["Zmiana"].get().strip()

            if not first_name or not last_name or not op_id or not shift:
                messagebox.showerror("Błąd", "Wszystkie pola są wymagane.")
                return

            self.store.add_operator(Operator(first_name=first_name, last_name=last_name, operator_id=op_id, shift=shift))
            dialog.destroy()
            self.refresh_operators_list()

        ttk.Button(dialog, text="Zapisz operatora", style="Action.TButton", command=save_operator).grid(row=5, column=0, columnspan=2, pady=24)


def main():
    app = BandiApp()
    app.mainloop()


if __name__ == "__main__":
    main()
