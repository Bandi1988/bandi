# -*- coding: utf-8 -*-
import json, os, calendar
from datetime import date, datetime
import tkinter as tk
from tkinter import ttk, messagebox, simpledialog
from pathlib import Path

APP_NAME="CLW Production Manager"
SHIFTS=["22:00–06:00","06:00–14:00","14:00–22:00"]
DEFAULT={"tls":["TL 1","TL 2"],"clw1_items":["KS","ASR1","ASR2","DIM JIG"],"clw2_items":["KS","ASR1","ASR2","DIM JIG"],"records":[]}
def data_path():
    p=Path(os.environ.get("LOCALAPPDATA",str(Path.home())))/"CLWProductionManager";p.mkdir(parents=True,exist_ok=True);return p/"data.json"
class Store:
    def __init__(self):
        self.path=data_path()
        try:self.data=json.loads(self.path.read_text(encoding="utf-8")) if self.path.exists() else json.loads(json.dumps(DEFAULT))
        except:self.data=json.loads(json.dumps(DEFAULT))
        for k,v in DEFAULT.items():self.data.setdefault(k,json.loads(json.dumps(v)))
        self.save()
    def save(self):self.path.write_text(json.dumps(self.data,ensure_ascii=False,indent=2),encoding="utf-8")
class App(tk.Tk):
    def __init__(self):
        super().__init__();self.title(APP_NAME);self.geometry("1180x760");self.minsize(980,650);self.configure(bg="#eef2f6");self.store=Store();self._style();self.show_home()
    def _style(self):
        s=ttk.Style(self)
        try:s.theme_use("clam")
        except:pass
        s.configure("TFrame",background="#eef2f6");s.configure("TLabel",background="#eef2f6",font=("Segoe UI",10));s.configure("Title.TLabel",font=("Segoe UI",22,"bold"),foreground="#172b4d");s.configure("Sub.TLabel",font=("Segoe UI",11),foreground="#526174");s.configure("Treeview",rowheight=29,font=("Segoe UI",10));s.configure("Treeview.Heading",font=("Segoe UI",10,"bold"));s.configure("TButton",font=("Segoe UI",10),padding=8)
    def clear(self):
        for w in self.winfo_children():w.destroy()
    def header(self,t,sub=""):
        f=ttk.Frame(self,padding=(22,18,22,10));f.pack(fill="x");ttk.Label(f,text=t,style="Title.TLabel").pack(anchor="w")
        if sub:ttk.Label(f,text=sub,style="Sub.TLabel").pack(anchor="w",pady=(4,0))
    def btn(self,p,t,cmd,color):
        return tk.Button(p,text=t,command=cmd,bg=color,fg="#17212f",activebackground=color,relief="flat",font=("Segoe UI",12,"bold"),cursor="hand2",padx=14,pady=15,wraplength=240)
    def home(self):self.show_home()
    def show_home(self):
        self.clear();self.header("CLW Production Manager","Lokální evidence výroby a analýza zmetků • automatické ukládání")
        a=tk.Frame(self,bg="#eef2f6");a.pack(expand=True)
        cards=[("CLW1  •  DATA","#f4df35",lambda:self.show_table("CLW1")),("CLW2  •  DATA","#f4df35",lambda:self.show_table("CLW2")),("CLW1  •  GRAF","#8dcc4f",lambda:self.show_graph("CLW1")),("CLW2  •  GRAF","#8dcc4f",lambda:self.show_graph("CLW2")),("CLW1  •  PŘIDAT DATA","#13a9df",lambda:self.add_record("CLW1")),("CLW2  •  PŘIDAT DATA","#13a9df",lambda:self.add_record("CLW2"))]
        for i,(t,c,fn) in enumerate(cards):r,col=divmod(i,2);self.btn(a,t,fn,c).grid(row=r,column=col,padx=18,pady=12,sticky="nsew",ipadx=36,ipady=14)
        for c in range(2):a.grid_columnconfigure(c,weight=1,minsize=300)
        b=ttk.Frame(self,padding=20);b.pack(fill="x");ttk.Button(b,text="⚙ Nastavení",command=self.show_settings).pack(side="left");ttk.Button(b,text="Umístění dat",command=self.about).pack(side="left",padx=8);ttk.Label(b,text="Data se ukládají automaticky na tomto počítači.",foreground="#526174").pack(side="right")
    def about(self):messagebox.showinfo("Umístění dat",f"Data jsou uložena zde:\n{self.store.path}\n\nZkopírujte data.json pro zálohu.")
    def key(self,clw):return clw.lower()+"_items"
    def show_settings(self):
        self.clear();self.header("Nastavení","Správa TL a výrobních položek.");body=ttk.Frame(self,padding=20);body.pack(fill="both",expand=True)
        l=ttk.LabelFrame(body,text="Team Leaders (TL)",padding=12);l.pack(side="left",fill="both",expand=True,padx=(0,10));r=ttk.LabelFrame(body,text="Položky výroby / defekty",padding=12);r.pack(side="left",fill="both",expand=True,padx=(10,0))
        self.tls=tk.Listbox(l,font=("Segoe UI",11));self.tls.pack(fill="both",expand=True)
        for x in self.store.data["tls"]:self.tls.insert("end",x)
        row=ttk.Frame(l);row.pack(fill="x",pady=8);ttk.Button(row,text="Přidat TL",command=self.add_tl).pack(side="left");ttk.Button(row,text="Odebrat vybraný",command=self.del_tl).pack(side="left",padx=6)
        self.setting_clw=tk.StringVar(value="CLW1");ttk.Label(r,text="Linka").pack(anchor="w");cb=ttk.Combobox(r,textvariable=self.setting_clw,values=["CLW1","CLW2"],state="readonly");cb.pack(fill="x");cb.bind("<<ComboboxSelected>>",lambda e:self.refresh_items())
        self.items=tk.Listbox(r,font=("Segoe UI",11));self.items.pack(fill="both",expand=True,pady=6);row=ttk.Frame(r);row.pack(fill="x");ttk.Button(row,text="Přidat položku",command=self.add_item).pack(side="left");ttk.Button(row,text="Odebrat vybranou",command=self.del_item).pack(side="left",padx=6)
        ttk.Label(r,text="První položka KS je základ pro výpočet procent.",wraplength=420).pack(anchor="w",pady=8);self.refresh_items();ttk.Button(self,text="← Menu",command=self.show_home).pack(anchor="w",padx=22,pady=14)
    def refresh_items(self):
        if hasattr(self,"items"):
            self.items.delete(0,"end")
            for x in self.store.data[self.key(self.setting_clw.get())]:self.items.insert("end",x)
    def add_tl(self):
        x=simpledialog.askstring("Nový TL","Zadejte jméno TL:",parent=self)
        if x and x.strip() and x.strip() not in self.store.data["tls"]:self.store.data["tls"].append(x.strip());self.store.save();self.tls.insert("end",x.strip())
    def del_tl(self):
        s=self.tls.curselection()
        if s and messagebox.askyesno("Potvrzení","Odebrat TL? Starší záznamy zůstanou."):self.store.data["tls"].pop(s[0]);self.store.save();self.tls.delete(s[0])
    def add_item(self):
        x=simpledialog.askstring("Nová položka","Název položky:",parent=self);k=self.key(self.setting_clw.get())
        if x and x.strip() and x.strip() not in self.store.data[k]:self.store.data[k].append(x.strip());self.store.save();self.refresh_items()
    def del_item(self):
        s=self.items.curselection();k=self.key(self.setting_clw.get())
        if s:
            if s[0]==0:messagebox.showwarning("Nelze","První položka KS je základ pro procenta.");return
            self.store.data[k].pop(s[0]);self.store.save();self.refresh_items()
    def pick_date(self,var):
        w=tk.Toplevel(self);w.title("Kalendář");w.transient(self);w.grab_set()
        try:d=datetime.strptime(var.get(),"%Y-%m-%d").date()
        except:d=date.today()
        st={"y":d.year,"m":d.month}
        def draw():
            for x in w.winfo_children():x.destroy()
            h=ttk.Frame(w,padding=8);h.pack();ttk.Button(h,text="◀",command=lambda:change(-1)).pack(side="left");ttk.Label(h,text=f"{calendar.month_name[st['m']]} {st['y']}").pack(side="left",padx=10);ttk.Button(h,text="▶",command=lambda:change(1)).pack(side="left")
            g=ttk.Frame(w,padding=8);g.pack()
            for c,x in enumerate(["Po","Út","St","Čt","Pá","So","Ne"]):ttk.Label(g,text=x,width=4).grid(row=0,column=c)
            for rr,week in enumerate(calendar.monthcalendar(st["y"],st["m"]),1):
                for c,n in enumerate(week):
                    if n:ttk.Button(g,text=str(n),width=4,command=lambda n=n:choose(n)).grid(row=rr,column=c,padx=1,pady=1)
        def change(n):
            st["m"]+=n
            if st["m"]==0:st.update(m=12,y=st["y"]-1)
            if st["m"]==13:st.update(m=1,y=st["y"]+1)
            draw()
        def choose(n):var.set(date(st["y"],st["m"],n).isoformat());w.destroy()
        draw()
    def add_record(self,clw):
        items=self.store.data[self.key(clw)]
        if not items:messagebox.showwarning("Chybí položky","Přidejte položky v nastavení.");return
        w=tk.Toplevel(self);w.title("Přidat data – "+clw);w.geometry("510x560");w.transient(self);w.grab_set();f=ttk.Frame(w,padding=18);f.pack(fill="both",expand=True)
        ttk.Label(f,text=f"Nový záznam • {clw}",font=("Segoe UI",17,"bold")).pack(anchor="w",pady=(0,12))
        dv=tk.StringVar(value=date.today().isoformat());sv=tk.StringVar(value=SHIFTS[0]);tv=tk.StringVar(value=self.store.data["tls"][0] if self.store.data["tls"] else "")
        ttk.Label(f,text="Datum (RRRR-MM-DD)").pack(anchor="w");ttk.Entry(f,textvariable=dv).pack(fill="x");ttk.Button(f,text="Vybrat z kalendáře",command=lambda:self.pick_date(dv)).pack(anchor="w",pady=5)
        ttk.Label(f,text="Směna").pack(anchor="w");ttk.Combobox(f,textvariable=sv,values=SHIFTS,state="readonly").pack(fill="x",pady=4)
        ttk.Label(f,text="TL").pack(anchor="w");ttk.Combobox(f,textvariable=tv,values=self.store.data["tls"],state="readonly").pack(fill="x",pady=4);ttk.Separator(f).pack(fill="x",pady=10)
        pos=ttk.Label(f,font=("Segoe UI",12,"bold"));pos.pack(anchor="w");prompt=ttk.Label(f);prompt.pack(anchor="w",pady=4);vv=tk.StringVar(value="0");ent=ttk.Entry(f,textvariable=vv,font=("Segoe UI",16));ent.pack(fill="x",pady=8);state={"i":0,"values":{}}
        def render():pos.config(text=f"Položka {state['i']+1} / {len(items)}");prompt.config(text="Počet kusů: "+items[state["i"]]);vv.set("0");ent.focus_set()
        def nxt():
            try:n=int(vv.get().strip() or 0);assert n>=0
            except:messagebox.showerror("Chyba","Zadejte celé číslo 0 nebo vyšší.",parent=w);return
            state["values"][items[state["i"]]]=n
            if state["i"]+1<len(items):state["i"]+=1;render();return
            try:datetime.strptime(dv.get(),"%Y-%m-%d")
            except:messagebox.showerror("Datum","Použijte RRRR-MM-DD.",parent=w);return
            self.store.data["records"].append({"id":datetime.now().strftime("%Y%m%d%H%M%S%f"),"clw":clw,"date":dv.get(),"shift":sv.get(),"tl":tv.get(),"values":state["values"]});self.store.save();w.destroy();messagebox.showinfo("Uloženo","Záznam uložen.")
        b=ttk.Frame(f);b.pack(fill="x",pady=12);ttk.Button(b,text="Zrušit",command=w.destroy).pack(side="left");ttk.Button(b,text="Další →",command=nxt).pack(side="right");ent.bind("<Return>",lambda e:nxt());render()
    def show_table(self,clw):
        self.clear();self.header(clw+" – DATA","Dvojklikem na řádek nebo tlačítkem upravíte záznam. Ukládá se automaticky.")
        bar=ttk.Frame(self,padding=(22,0));bar.pack(fill="x");ttk.Button(bar,text="＋ Přidat data",command=lambda:self.add_record(clw)).pack(side="left");ttk.Button(bar,text="Upravit vybraný",command=lambda:self.edit_selected(tree,clw)).pack(side="left",padx=6);ttk.Button(bar,text="Smazat vybraný",command=lambda:self.delete_selected(tree)).pack(side="left");ttk.Button(bar,text="← Menu",command=self.show_home).pack(side="right")
        cols=["date","shift","tl"]+self.store.data[self.key(clw)];box=ttk.Frame(self,padding=22);box.pack(fill="both",expand=True);tree=ttk.Treeview(box,columns=cols,show="headings")
        for c in cols:tree.heading(c,text={"date":"Datum","shift":"Směna","tl":"TL"}.get(c,c));tree.column(c,width=115,anchor="center")
        vs=ttk.Scrollbar(box,orient="vertical",command=tree.yview);hs=ttk.Scrollbar(box,orient="horizontal",command=tree.xview);tree.configure(yscrollcommand=vs.set,xscrollcommand=hs.set);tree.grid(row=0,column=0,sticky="nsew");vs.grid(row=0,column=1,sticky="ns");hs.grid(row=1,column=0,sticky="ew");box.rowconfigure(0,weight=1);box.columnconfigure(0,weight=1);self.fill_tree(tree,clw,cols);tree.bind("<Double-1>",lambda e:self.edit_selected(tree,clw))
    def fill_tree(self,t,clw,cols):
        t.delete(*t.get_children())
        for r in sorted([x for x in self.store.data["records"] if x.get("clw")==clw],key=lambda x:(x.get("date",""),x.get("shift",""))):
            t.insert("","end",iid=r["id"],values=[r.get("date",""),r.get("shift",""),r.get("tl","")]+[r.get("values",{}).get(x,0) for x in cols[3:]])
    def edit_selected(self,t,clw):
        s=t.selection()
        if not s:return
        rec=next((x for x in self.store.data["records"] if x["id"]==s[0]),None)
        if not rec:return
        w=tk.Toplevel(self);w.title("Upravit záznam");w.geometry("430x560");w.transient(self);w.grab_set();f=ttk.Frame(w,padding=16);f.pack(fill="both",expand=True)
        dv=tk.StringVar(value=rec["date"]);sv=tk.StringVar(value=rec["shift"]);tv=tk.StringVar(value=rec["tl"]);ttk.Label(f,text="Datum RRRR-MM-DD").pack(anchor="w");ttk.Entry(f,textvariable=dv).pack(fill="x");ttk.Label(f,text="Směna").pack(anchor="w");ttk.Combobox(f,textvariable=sv,values=SHIFTS,state="readonly").pack(fill="x");ttk.Label(f,text="TL").pack(anchor="w");ttk.Combobox(f,textvariable=tv,values=self.store.data["tls"]).pack(fill="x")
        vals={}
        for x in self.store.data[self.key(clw)]:ttk.Label(f,text=x).pack(anchor="w",pady=(5,0));vals[x]=tk.StringVar(value=str(rec.get("values",{}).get(x,0)));ttk.Entry(f,textvariable=vals[x]).pack(fill="x")
        def save():
            try:datetime.strptime(dv.get(),"%Y-%m-%d");data={k:int(v.get() or 0) for k,v in vals.items()};assert all(n>=0 for n in data.values())
            except:messagebox.showerror("Chyba","Zkontrolujte datum a počty.",parent=w);return
            rec.update(date=dv.get(),shift=sv.get(),tl=tv.get(),values=data);self.store.save();w.destroy();self.show_table(clw)
        ttk.Button(f,text="Uložit změny",command=save).pack(pady=14)
    def delete_selected(self,t):
        s=t.selection()
        if s and messagebox.askyesno("Smazat","Opravdu smazat vybraný záznam?"):
            self.store.data["records"]=[r for r in self.store.data["records"] if r["id"]!=s[0]];self.store.save();t.delete(s[0])
    def show_graph(self,clw):
        self.clear();self.header(clw+" – GRAF","1 defekt = 48 sekund = 0,8 minuty.")
        c=ttk.Frame(self,padding=(22,0));c.pack(fill="x");period=tk.StringVar(value="Měsíční");shift=tk.StringVar(value="Všechny směny");tl=tk.StringVar(value="Všichni TL");yr=tk.StringVar(value=str(date.today().year));mo=tk.StringVar(value=str(date.today().month));day=tk.StringVar(value=date.today().isoformat());d1=tk.StringVar(value=date.today().replace(day=1).isoformat());d2=tk.StringVar(value=date.today().isoformat())
        ttk.Label(c,text="Období").pack(side="left");ttk.Combobox(c,textvariable=period,values=["Denní","Měsíční","Roční","Vlastní"],state="readonly",width=10).pack(side="left",padx=3);ttk.Label(c,text="Rok").pack(side="left");ttk.Entry(c,textvariable=yr,width=6).pack(side="left");ttk.Label(c,text="Měsíc").pack(side="left");ttk.Entry(c,textvariable=mo,width=3).pack(side="left");ttk.Label(c,text="Směna").pack(side="left");ttk.Combobox(c,textvariable=shift,values=["Všechny směny"]+SHIFTS,state="readonly",width=14).pack(side="left");ttk.Label(c,text="TL").pack(side="left");ttk.Combobox(c,textvariable=tl,values=["Všichni TL"]+self.store.data["tls"],state="readonly",width=12).pack(side="left")
        d=ttk.Frame(self,padding=(22,7));d.pack(fill="x");ttk.Label(d,text="Den").pack(side="left");ttk.Entry(d,textvariable=day,width=12).pack(side="left",padx=3);ttk.Label(d,text="Od").pack(side="left");ttk.Entry(d,textvariable=d1,width=12).pack(side="left",padx=3);ttk.Label(d,text="Do").pack(side="left");ttk.Entry(d,textvariable=d2,width=12).pack(side="left",padx=3)
        body=ttk.Frame(self,padding=22);body.pack(fill="both",expand=True);table=ttk.Treeview(body,columns=("item","qty","pct","loss"),show="headings",height=7)
        for k,title,width in [("item","Položka",220),("qty","Počet ks",130),("pct","% z KS",130),("loss","Ztráta (min)",150)]:table.heading(k,text=title);table.column(k,width=width,anchor="center")
        table.pack(fill="x");chart=tk.Canvas(body,bg="white",highlightthickness=1,highlightbackground="#d7dee8",height=300);chart.pack(fill="both",expand=True,pady=12)
        def update():
            try:y=int(yr.get());m=int(mo.get());assert 1<=m<=12
            except:messagebox.showerror("Filtr","Neplatný rok nebo měsíc.");return
            rs=[r for r in self.store.data["records"] if r.get("clw")==clw]
            if shift.get()!="Všechny směny":rs=[r for r in rs if r["shift"]==shift.get()]
            if tl.get()!="Všichni TL":rs=[r for r in rs if r["tl"]==tl.get()]
            mode=period.get()
            try:
                if mode=="Denní":rs=[r for r in rs if r["date"]==datetime.strptime(day.get(),"%Y-%m-%d").date().isoformat()]
                elif mode=="Měsíční":rs=[r for r in rs if r["date"].startswith(f"{y:04d}-{m:02d}")]
                elif mode=="Roční":rs=[r for r in rs if r["date"].startswith(str(y))]
                else:
                    a=datetime.strptime(d1.get(),"%Y-%m-%d").date();b=datetime.strptime(d2.get(),"%Y-%m-%d").date()
                    if b<a:raise ValueError()
                    rs=[r for r in rs if a.isoformat()<=r["date"]<=b.isoformat()]
            except:messagebox.showerror("Filtr","Zkontrolujte zadaná data ve formátu RRRR-MM-DD.");return
            its=self.store.data[self.key(clw)];tot={k:sum(int(r.get("values",{}).get(k,0)) for r in rs) for k in its};ks=tot.get(its[0],0) if its else 0;table.delete(*table.get_children());labels=[];numbers=[]
            for k in its[1:]:
                q=tot[k];table.insert("","end",values=(k,q,f"{q/ks*100:.2f} %" if ks else "0.00 %",f"{q*.8:.1f}"));labels.append(k);numbers.append(q)
            chart.delete("all");w=max(chart.winfo_width(),600);h=max(chart.winfo_height(),260)
            if not labels:chart.create_text(w/2,h/2,text="Pro vybrané období nejsou data.",font=("Segoe UI",13),fill="#526174");return
            maxv=max(numbers+[1]);left=55;bottom=h-45;top=35;slot=(w-left-25)/len(labels);bw=min(75,slot*.55);chart.create_text(left,10,text=f"KS: {ks}  |  Ztráta: {sum(numbers)*.8:.1f} min",anchor="nw",font=("Segoe UI",10,"bold"),fill="#172b4d");chart.create_line(left,bottom,w-20,bottom,fill="#8392a5")
            for i,(lab,n) in enumerate(zip(labels,numbers)):
                x=left+slot*i+slot/2;bh=(bottom-top)*n/maxv;chart.create_rectangle(x-bw/2,bottom-bh,x+bw/2,bottom,fill="#17a6d9",outline="");chart.create_text(x,bottom-bh-12,text=str(n),font=("Segoe UI",9,"bold"));chart.create_text(x,bottom+18,text=lab,font=("Segoe UI",9))
        ttk.Button(c,text="Zobrazit graf",command=update).pack(side="right");ttk.Button(c,text="← Menu",command=self.show_home).pack(side="right",padx=8);self.after(200,update)
if __name__=="__main__":App().mainloop()
