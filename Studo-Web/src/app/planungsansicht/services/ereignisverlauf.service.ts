import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { EreignisTyp } from './aenderungsverlauf.service';
import { forkJoin, map, of, switchMap } from 'rxjs';
 
//------------------- Dtos ------------------------------------
 
export interface EreignisDTO {
    id: number;
    typ: EreignisTyp;
    startdatum: string;        
    enddatum: string | null;
    ganztag: boolean;
    startzeitId: number | null;   
    endzeitId: number | null;    
    erstellungsdatum: string;    
    autorId: number | null;
    lehrkraftId: number | null;   
    raumId: number | null;        
    lerngruppeId: number | null;  
    fachId: number | null;        
    grund: string | null;         
}
 
//Nachschlagedaten, damit statt IDs Namen angezeigt werden
export interface LehrkraftKurzDTO {
    id: number;
    vorname: string;
    nachname: string;
    kuerzel: string;
}
 
export interface RaumDTO {
    id: number;
    bezeichnung: string;
}
 
export interface LerngruppeKurzDTO {
    id: number;
    bezeichnung: string;
    jahrgangsnummer: number;
}
 
export interface FachDTO {
    id: number;
    kuerzel: string;
    name: string;
}
 
export interface ZeitslotDTO {
    id: number;
    stundennummer: number;
    start: string;
    ende: string;
}

interface SeiteDTO<T> {
    inhalt: T[];
    seite: number;
    groesse: number;
    gesamtElemente: number;
    gesamtSeiten: number;
}
 
 
//----------------------- Ereignisverlauf Methoden -----------------------------------------------
 
 
@Injectable({ providedIn: 'root' })
export class EreignisverlaufService {
    private http = inject(HttpClient);
    private baseUrl = environment.apiUrl;
 
    private ereignisse = signal<EreignisDTO[]>([]);
    public readonly alleEreignisse = this.ereignisse.asReadonly();
 
    private laden = signal(false);
    public readonly laedt = this.laden.asReadonly();
 
    private fehler = signal(false);
    public readonly hatFehler = this.fehler.asReadonly();
    
    //Backend liefert seitenweise -> alle Seiten laden und zusammenfügen, neueste zuerst
    ladeEreignisse() {
        this.laden.set(true);
        this.fehler.set(false);
        const url = `${this.baseUrl}/aenderungsverlauf/ereignisse`;

        this.http.get<SeiteDTO<EreignisDTO>>(url, { params: { seite: 0, groesse: 100 } }).pipe(
            switchMap((erste) => {
                if (erste.gesamtSeiten <= 1) return of(erste.inhalt);
                const rest = Array.from({ length: erste.gesamtSeiten - 1 }, (_, i) =>
                    this.http.get<SeiteDTO<EreignisDTO>>(url, { params: { seite: i + 1, groesse: erste.groesse } })
                        .pipe(map((s) => s.inhalt)));
                return forkJoin(rest).pipe(map((seiten) => [erste.inhalt, ...seiten].flat()));
            }),
            map((liste) => [...new Map(liste.map((e) => [e.id, e])).values()]),
        ).subscribe({
            next: (daten) => {
                this.ereignisse.set(daten.sort((a, b) =>
                    new Date(b.erstellungsdatum).getTime() - new Date(a.erstellungsdatum).getTime()));
                this.laden.set(false);
            },
            error: (err) => {
                console.error('Fehler beim Laden der Ereignisse:', err);
                this.fehler.set(true);
                this.laden.set(false);
            },
        });
    }
    
 
    //---------------- Nachschlagedaten (IDs -> Namen) ---------------------------------------
 
 
    private lehrkraefte = signal<LehrkraftKurzDTO[]>([]);
    public readonly alleLehrkraefte = this.lehrkraefte.asReadonly();
 
    private raeume = signal<RaumDTO[]>([]);
    public readonly alleRaeume = this.raeume.asReadonly();
 
    private lerngruppen = signal<LerngruppeKurzDTO[]>([]);
    public readonly alleLerngruppen = this.lerngruppen.asReadonly();
 
    private faecher = signal<FachDTO[]>([]);
    public readonly alleFaecher = this.faecher.asReadonly();
 
    private zeitslots = signal<ZeitslotDTO[]>([]);
    public readonly alleZeitslots = this.zeitslots.asReadonly();
 
    //schlägt eine Anfrage fehl, wird dort einfach die ID angezeigt
    ladeNachschlagedaten() {
        this.http.get<LehrkraftKurzDTO[]>(`${this.baseUrl}/schulansicht/lehrkraefte`).subscribe({
            next: (daten) => this.lehrkraefte.set(daten),
            error: (err) => console.error('Fehler beim Laden der Lehrkräfte:', err),
        });
 
        this.http.get<RaumDTO[]>(`${this.baseUrl}/raeume`).subscribe({
            next: (daten) => this.raeume.set(daten),
            error: (err) => console.error('Fehler beim Laden der Räume:', err),
        });
 
        this.http.get<LerngruppeKurzDTO[]>(`${this.baseUrl}/schulansicht/lerngruppen`).subscribe({
            next: (daten) => this.lerngruppen.set(daten),
            error: (err) => console.error('Fehler beim Laden der Klassen:', err),
        });
 
        this.http.get<FachDTO[]>(`${this.baseUrl}/dashboard/faecher`).subscribe({
            next: (daten) => this.faecher.set(daten),
            error: (err) => console.error('Fehler beim Laden der Fächer:', err),
        });
 
        this.http.get<ZeitslotDTO[]>(`${this.baseUrl}/dashboard/zeitslots`).subscribe({
            next: (daten) => this.zeitslots.set(daten),
            error: (err) => console.error('Fehler beim Laden der Zeitslots:', err),
        });
    }
}