import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subscription, catchError, forkJoin, map, of, switchMap, tap, timer } from 'rxjs';
import { environment } from '../../../environments/environment';



export type EreignisTyp = 'AUSFALL' | 'RAUMAENDERUNG' | 'ZUSATZSTUNDE';
 

export interface OffeneAenderungDTO {
  id: number;
  typ: EreignisTyp;
  titel: string | null;
  beschreibung: string | null;
  ereignisId: number | null;
  unterrichtsstundeId: number | null;
  klassenstufeId: number;
  klasseId: number;
  erstelltAm: string; 
}
 

export interface AbgeschlosseneAenderungDTO {
  id: number;
  typ: EreignisTyp;          
  anzeigeTyp: string | null; 
  betroffeneEinheit: string;
  aenderung: string;        
  grund?: string | null;   
  eingetragenVon?: string | null; 
  zeitstempel: string;
  freistunde: boolean;
}

interface SeiteDTO<T> {
  inhalt: T[];
  seite: number;
  groesse: number;
  gesamtElemente: number;
  gesamtSeiten: number;
}
 
//--------------- Services ------------------------
 
const API = `${environment.apiUrl}/aenderungsverlauf`;
const URL_OFFEN = `${API}/offen`;
const URL_OFFEN_ANZAHL = `${API}/offen/anzahl`;
const URL_ABGESCHLOSSEN = `${API}/abgeschlossen`;
 
const POLLING_INTERVALL_MS = 30_000;
 
@Injectable({ providedIn: 'root' })
export class AenderungsverlaufService {
  private readonly http = inject(HttpClient);
  private pollingSub?: Subscription;
 
  // Gemeinsame Quelle für Änderungsverlauf +  Glocke  
  private readonly _offene = signal<OffeneAenderungDTO[]>([]);
  private readonly _anzahlOffen = signal(0);
  private readonly _offeneLaden = signal(false);
  private readonly _offeneFehler = signal(false);
 
  readonly offeneAenderungen = this._offene.asReadonly();
  readonly anzahlOffen = this._anzahlOffen.asReadonly();
  readonly offeneLaden = this._offeneLaden.asReadonly();
  readonly offeneFehler = this._offeneFehler.asReadonly();
 

 //------------------- Offene Änderungen ---------------------------

  offeneAktualisieren(): void {
    this._offeneLaden.set(true);
    this.http
      .get<OffeneAenderungDTO[]>(URL_OFFEN)
      .pipe(
        map((liste) => [...liste].sort((a, b) => zeit(b.erstelltAm) - zeit(a.erstelltAm))),
        catchError(() => of(null)),
      )
      .subscribe((liste) => {
        this._offeneLaden.set(false);
        if (liste === null) {
          //alte Daten behalten damit Glocke nicht auf 0 springt
          this._offeneFehler.set(true);
          return;
        }
        this._offeneFehler.set(false);
        this._offene.set(liste);
        this._anzahlOffen.set(liste.length);
      });
  }
 
  
   //fragt alle 30 s ab -> weicht die Zahl von der geladenen Liste ab,
   //wird die Liste nachgeladen -> Glocke und Status zeigen immer dasselbe
  startePolling(): void {
    if (this.pollingSub) return;
    this.pollingSub = timer(0, POLLING_INTERVALL_MS)
      .pipe(switchMap(() => this.holeAnzahl()))
      .subscribe((anzahl) => {
        if (anzahl === null) return;
        if (anzahl !== this._offene().length) {
          this.offeneAktualisieren();
        } else {
          this._anzahlOffen.set(anzahl);
        }
      });
  }
 
  //Logout 
  stoppePolling(): void {
    this.pollingSub?.unsubscribe();
    this.pollingSub = undefined;
  }
 
  //auf Vertretungsplanseite wenn gespeichert wird, wird von offen nach abgeschlossen geschoben -> Glocke verändert sich mit
  alsErledigtMarkieren(aenderungId: number): Observable<void> {
    return this.http.put<void>(`${URL_OFFEN}/${aenderungId}/erledigt`, null).pipe(
      tap(() => {
        this._offene.update((liste) => liste.filter((a) => a.id !== aenderungId));
        this._anzahlOffen.update((n) => Math.max(0, n - 1));
        this.offeneAktualisieren(); 
      }),
    );
  }
 
  //------------------ Abgeschlossene Änderungen -----------------------------------

  
//Backend liefert seitenweise -> alle Seiten laden und zusammenfügen
ladeAbgeschlossene(): Observable<AbgeschlosseneAenderungDTO[]> {
  return this.http.get<SeiteDTO<AbgeschlosseneAenderungDTO>>(URL_ABGESCHLOSSEN, { params: { seite: 0, groesse: 100 } })
    .pipe(switchMap((erste) => { if (erste.gesamtSeiten <= 1) return of(erste.inhalt);
        
      const rest = Array.from({ length: erste.gesamtSeiten - 1 }, (_, i) =>
          this.http.get<SeiteDTO<AbgeschlosseneAenderungDTO>>(URL_ABGESCHLOSSEN, {
              params: { seite: i + 1, groesse: erste.groesse },
            }).pipe(map((s) => s.inhalt)),
        );
        return forkJoin(rest).pipe(map((seiten) => [erste.inhalt, ...seiten].flat()));
      }),
      //doppelte Einträge entfernen, neueste zuerst
      map((liste) => [...new Map(liste.map((e) => [e.id, e])).values()]),
      map((liste) => liste.sort((a, b) => zeit(b.zeitstempel) - zeit(a.zeitstempel))),
    );
}
 
 
  private holeAnzahl(): Observable<number | null> {
    return this.http.get<number>(URL_OFFEN_ANZAHL).pipe(
      map((antwort) => {
        const n = Number(antwort);
        return Number.isFinite(n) ? n : null;
      }),
      catchError(() => of(null)),
    );
  }
}
 
function zeit(iso: string): number {
  return new Date(iso).getTime();
}