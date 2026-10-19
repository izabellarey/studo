import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

// Entspricht RaumDTO.java im Backend.
// WICHTIG: "art" muss exakt einer Konstante des Raumart-Enums entsprechen (Groß-/Kleinschreibung beachten).
export interface Raum {
  id: number|null;
  bezeichnung: string;
  art: RaumArt;
  kapazitaet: number;
}

export interface RaumAnlegen {
  bezeichnung: string;
  art: RaumArt;
  kapazitaet: number;
}

// Entspricht RaumplanEintragDTO.java im Backend
export interface RaumplanEintrag {
  wochentag: string;
  stundennummer: number;
  start: string;
  ende: string;
  fachName: string;
  lehrkraftKuerzel: string;
  lerngruppeKuerzel: string;
  geaendert: boolean;
  freistunde: boolean;
}
// Werte müssen exakt den Konstanten des Java-Enums entsprechen
export const RAUMARTEN = ['KLASSENRAUM', 'BIOLOGIERAUM', 'PHYSIKRAUM' ,'CHEMIERAUM' ,'KUNSTRAUM' ,'MUSIKRAUM' ,'INFORMATIKRAUM' ,'SPORTHALLE' ,'AULA' ] as const;
export type RaumArt = typeof RAUMARTEN[number];

@Injectable({ providedIn: 'root' })
export class RaumService {
  private apiUrl = `${environment.apiUrl}/raeume`;

  private http = inject(HttpClient);

  private raeumeSignal = signal<Raum[]>([]);
  public alleRaeume = this.raeumeSignal.asReadonly(); //Readonly = lesen, aber nicht verändern

  //Fragt nach Liste der Räume aus dem backend. 
  ladeRaeume() {
    this.http.get<Raum[]>(this.apiUrl).subscribe({
      next: (raeume) => this.raeumeSignal.set(raeume),
      error: (err) => console.error('Fehler beim Laden der Räume', err)
    });
  }

  // Legt den neuen Raum an und schickt die daten ans backend
  addRaum(dto:RaumAnlegen) {
    this.http.post<Raum>(this.apiUrl, dto).subscribe({
      next: (gespeichert) => this.raeumeSignal.update(raeume => [...raeume, gespeichert]),
      error: (err) => console.error('Fehler beim Anlegen des Raums', err)
    });
  }

  // Ruft den Raumplan für einen Raum ab
  raumplanAbrufen(raumId: number) {
    return this.http.get<RaumplanEintrag[]>(`${this.apiUrl}/${raumId}`);
  }
}