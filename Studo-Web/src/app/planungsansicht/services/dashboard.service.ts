//Imports
import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export type LocalDate = string;
export type LocalTime = string;
export type LocalDateTime = string;

export type EreignisAnlegenDTO = EreignisLehrkraftausfallDTO | EreignisRaumaenderungDTO | EreignisZusatzstundeDTO;

//Enums
export enum Raumart {
  KLASSENRAUM = 'KLASSENRAUM',
  BIOLOGIERAUM = 'BIOLOGIERAUM',
  PHYSIKRAUM = 'PHYSIKRAUM',
  CHEMIERAUM = 'CHEMIERAUM',
  KUNSTRAUM = 'KUNSTRAUM',
  MUSIKRAUM = 'MUSIKRAUM',
  INFORMATIKRAUM = 'INFORMATIK',
  SPORTHALLE = 'SPORTHALLE',
  AULA = 'AULA'
}

export enum EreignisTyp {
  RAUMAENDERUNG = 'RAUMAENDERUNG',
  AUSFALL = 'AUSFALL',
  ZUSATZSTUNDE = 'ZUSATZSTUNDE',
}

//DTOs
export interface Zeitslot {
  id: number;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
}

export interface Fach {
  id: number;
  kuerzel: string;
  name: string;
}

export interface Raum {
  id: number;
  bezeichnung: string;
  art: Raumart;
  kapazitaet: number;
}

export interface VerfuegbareRaeumeAnfragen {
  zeitslotId: number;
  fachId: number;
  startdatum: LocalDateTime;
}

export interface EreignisLehrkraftausfallDTO {
  typ: EreignisTyp;
  startdatum: LocalDate;
  enddatum: LocalDate;
  ganztag: boolean;
  startzeitId: number | null;
  endzeitId: number | null;
  erstellungsdatum: LocalDateTime;
  lehrkraftId: number;
  grund: string;
}

export interface EreignisRaumaenderungDTO {
  typ: EreignisTyp;
  startdatum: LocalDate;
  enddatum: LocalDate;
  ganztag: boolean;
  startzeitId: number | null;
  endzeitId: number | null;
  erstellungsdatum: LocalDateTime;
  raumId: number;
}

export interface EreignisZusatzstundeDTO {
  typ: EreignisTyp;
  startdatum: LocalDate;
  enddatum: LocalDate;
  startzeitId: number | null;
  lerngruppeId: number;
  fachId: number;
  raumId: number;
  erstellungsdatum: LocalDateTime
}

export interface EreignisDTO {
  id: number;
  typ: EreignisTyp;
  startdatum: LocalDate;
  enddatum: LocalDate;
  ganztag: boolean;
  startzeitId: number | null;
  endzeitId: number | null;
  erstellungsdatum: LocalDateTime;
  authorId: number;
  lehrkraftId: number | null;
  raumId: number | null;
  lerngruppenId: number | null;
  fachId: number | null;
  grund: string | null;
}

export interface DashboardDTO {
  anzahlOffeneEreignisse: number;
  aktuelleAenderungen: AktuelleAenderungDTO[];
}

export interface AktuelleAenderungDTO {
  id: number;
  typ: EreignisTyp;
  titel: string;
  beschreibung: string;
  offen: boolean;

  ereignisId: number;
  unterrichtsstundeId: number;
  klassenstufeId: number;
  klasseId: number;
  erstelltAm: LocalDateTime;
}

export interface LerngruppeUebersichtDTO {
  id: number;
  bezeichnung: string;
  jahrgangsnummer: number;
}

export interface KlassenstufeUebersichtDTO {
  id: number; 
  nummer: number;
  anzahlKlassen: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {

  //HTTP-Client injizieren, um HTTP-Anfragen ans Backend zu schicken
  private http = inject(HttpClient); 
  private startUrl = `${environment.apiUrl}/dashboard`; 

  //Vraiablen Festlegen
  public dashboard = signal<DashboardDTO | null>(null);
  public readonly Dashboard = this.dashboard.asReadonly();

  private zeitslot = signal<Zeitslot[]>([]);
  public readonly alleZeitslots = this.zeitslot.asReadonly();

  private fach = signal<Fach[]>([]);
  public readonly alleFaecher = this.fach.asReadonly();

  private verfuegbareRaeume = signal<Raum[]>([]);
  public readonly alleVerfuegbarenRaeume = this.verfuegbareRaeume.asReadonly();

  private neuesEreignis = signal<EreignisDTO | null>(null);
  public readonly alleNeuenEreignisse = this.neuesEreignis.asReadonly();

  private klassenstufen = signal<KlassenstufeUebersichtDTO[]>([]);
  public readonly alleKlassenstufen = this.klassenstufen.asReadonly();

  private lerngruppe = signal<LerngruppeUebersichtDTO[]>([]);
  public readonly alleLerngruppen = this.lerngruppe.asReadonly();

  //Lademethoden
  dashboardLaden() {
    this.http.get<DashboardDTO>(this.startUrl).subscribe({
        next: (daten) => this.dashboard.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  zeitslotsLaden() {
      this.http.get<Zeitslot[]>(`${this.startUrl}/zeitslots`).subscribe({
          next: (daten) => this.zeitslot.set(daten),
          error: (err) => console.error('Fehler beim Laden:', err),  
      });
  }

  faecherLaden() {
    this.http.get<Fach[]>(`${this.startUrl}/faecher`).subscribe({
        next: (daten) => this.fach.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  lerngruppeLaden(): void {
    this.http.get<LerngruppeUebersichtDTO[]>(`${environment.apiUrl}/stundenplaene/klassen`).subscribe({
      next: (daten) => this.lerngruppe.set(daten),
      error: (err) => console.error('Fehler beim Laden der Lerngruppe:', err)
    });
  }

  //Request
  verfuegbareRaeumeAnfragen(anfrage: VerfuegbareRaeumeAnfragen) {
    this.http.request<Raum[]>('GET', `${this.startUrl}/verfuegbareRaeume`,{body: anfrage}).subscribe({
      next: (daten) => {this.verfuegbareRaeume.set(daten);},
      error: (err) => {console.error('Fehler beim Laden der verfügbaren Räume:', err);}
    });
  }

  //Neu anlegen
  ereignisAnlegen(dto: EreignisAnlegenDTO) {
    this.http.post<EreignisDTO>(`${environment.apiUrl}/ereignisse`, dto).subscribe({
      next: (daten) => {
        this.dashboardLaden();
        this.neuesEreignis.set(daten)
      },
      error: (err) => console.error('Fehler beim Anlegen des Ereignisses:', err)
    });
  }

  //nach erledigung dashboard neu laden und offene Änderungen aktualisiert
  aenderungAlsErledigtMarkieren(id: number) {
    this.http.put<void>(`${environment.apiUrl}/aenderungsverlauf/offen/${id}/erledigt`,{}).subscribe({
      next: () => { this.dashboardLaden(); },
      error: (err) => console.error('Fehler beim Abschließen der Änderung:', err)
    });
  }

}