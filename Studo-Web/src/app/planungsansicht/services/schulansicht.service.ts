//Imports
import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LocalDate } from './dashboard.service';

export type LocalTime = string;
export type Schuljahr = string;

//Enums
export enum EreignisTyp {
  RAUMAENDERUNG = 'RAUMAENDERUNG',
  AUSFALL = 'AUSFALL',
  ZUSATZSTUNDE = 'ZUSATZSTUNDE',
}

export enum Wochentag {
  MONTAG = 'MONTAG',
  DIENSTAG = 'DIENSTAG',
  MITTWOCH = 'MITTWOCH',
  DONNERSTAG = 'DONNERSTAG',
  FREITAG = 'FREITAG'
}

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

//DTOs
export interface ArchivDTO {
    schuljahr: string;
    halbjahr: number;
    lerngruppeBezeichnung: string;
    gueltigStart: LocalDate;
    gueltigEnde: LocalDate;
    erstellerVorname: string;
    erstellerNachname: string; 
    unterrichtsstunde: UnterrichtsstundeDTO[];
}

export interface Lehrkraefte {
  id: number;
  vorname: string;
  nachname: string;
  kuerzel: string;
}

export interface Lerngruppen {
  id: number;
  bezeichnung: string;
  jahrgangsnummer: number;
}

export interface UnterrichtsstundeDTO {
  id: number;
  wochentag: Wochentag;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
  fachName: string;
  raumBezeichnung: string;
  lehrkraftKuerzel: string;
  lerngruppenKuerzel: string;
  geaendert: boolean;
  freistunde: boolean;
  offeneAenderung: boolean;
  grund: EreignisTyp | null;
}

export interface RaumDTO {
  id: number;
  bezeichnung: string;
  art: Raumart;
  kapazitaet: number;
}

export interface RaumplanDTO {
  wochentag: Wochentag;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
  fachName: string;
  lehrkraftKuerzel: string;
  lerngruppenKuerzel: string;
  geaendert: boolean;
}

export interface Jahreswechsel {
  jahreswechsel: string;
}

export interface ZeitslotDTO {
  id: number;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
}

export interface PauseDTO {
  id?: number;
  nachStunde: number;
  dauerMinuten: number;
}

export interface StundenrasterDTO {
  id: number;
  schulbeginn: LocalTime;
  stundendauer: number;
  stundenProTag: number;
  pausen: PauseDTO[];
  zeitslots: ZeitslotDTO[];
}

@Injectable({ providedIn: 'root' })
export class SchulansichtService {

  //HTTP-Client injizieren, um HTTP-Anfragen ans Backend zu schicken
  private http = inject(HttpClient); 
  private startUrl = `${environment.apiUrl}/schulansicht`; 

  //Variablen deklarieren
  private schulansicht = signal<Schuljahr | null>(null);
  public readonly schulansichten = this.schulansicht.asReadonly();

  private lehrkraefte = signal<Lehrkraefte[]>([]);
  public readonly alleLehrkraefte = this.lehrkraefte.asReadonly();

  private stundenplan = signal<UnterrichtsstundeDTO[]>([]);
  public readonly alleStundenplaene = this.stundenplan.asReadonly();

  private raum = signal<RaumDTO[]>([]);
  public readonly alleRaeume = this.raum.asReadonly();

  private raumplan = signal<RaumplanDTO[]>([]);
  public readonly alleRaumplaene = this.raumplan.asReadonly();

  private lerngruppe = signal<Lerngruppen[]>([]);
  public readonly alleLerngruppen = this.lerngruppe.asReadonly();

  public archiv = signal<ArchivDTO[]>([]);

  private stundenraster = signal<StundenrasterDTO | null>(null);
  public readonly aktuellesStundenraster = this.stundenraster.asReadonly();

  //Laden
  //gibt nur das aktuelle Schuljahr zurück
  schulansichtLaden() {
    this.http.get(this.startUrl, {
      responseType: 'text'
    }).subscribe({
      next: (daten) => { this.schulansicht.set(daten);},
      error: (err) => { console.error('Fehler beim Laden des Schuljahres:', err);},
    });
  }

  lehrkraftLaden() {
    this.http.get<Lehrkraefte[]>(`${this.startUrl}/lehrkraefte`).subscribe({
        next: (daten) => this.lehrkraefte.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  stundenplanLehrkraftLaden(id: number) {
    this.http.get<UnterrichtsstundeDTO[]>(`${this.startUrl}/lehrkraefte/${id}`).subscribe({
        next: (daten) => this.stundenplan.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  raeumeLaden() {
    this.http.get<RaumDTO[]>(`${this.startUrl}/raeume`).subscribe({
        next: (daten) => this.raum.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  raumplanLaden(id: number) {
    this.http.get<RaumplanDTO[]>(`${this.startUrl}/raeume/${id}`).subscribe({
        next: (daten) => this.raumplan.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  lerngruppenLaden() {
    this.http.get<Lerngruppen[]>(`${this.startUrl}/lerngruppen`).subscribe({
        next: (daten) => this.lerngruppe.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  stundenplanLerngruppeLaden(id: number) {
    this.http.get<UnterrichtsstundeDTO[]>(`${this.startUrl}/lerngruppen/${id}`).subscribe({
        next: (daten) => this.stundenplan.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  archivLaden() {
    this.http.get<ArchivDTO[]>(`${this.startUrl}/archiv`).subscribe({
        next: (daten) => this.archiv.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  stundenrasterLaden() {
    this.http.get<StundenrasterDTO>(`${environment.apiUrl}/stammdaten/stundenraster`).subscribe({
      next: (daten) => {this.stundenraster.set(daten);},
      error: (err) => {console.error('Fehler beim Laden des Stundenrasters:', err);}
    });
  }

  //ohne subscribe, damit zuerst der jahreswechsel durchgeführt wird, bevor alles neu geladen wird
  jahreswechselDurchfuehren() {
    return this.http.put<string>(`${this.startUrl}/jahreswechsel`, null);
  }

}