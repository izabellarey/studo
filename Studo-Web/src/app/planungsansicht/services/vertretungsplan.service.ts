//Imports
import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

export type LocalTime = string;
export type LocalDateTime = string;

//Enum
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
  FREITAG = 'FREITAG',
}

//DTOs
export interface UnterrichtsstundeDTO {
  id: number;
  wochentag: Wochentag;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
  fachName: string;
  raumBezeichnung: string;
  lehrkraftKuerzel: string;
  lerngruppeKuerzel: string;
  geaendert: boolean;
  freistunde: boolean;
  offeneAenderung: boolean;
  grund: EreignisTyp | null; //wichtig, weil die Stunden, die nicht von Ereignis betroffen sind keinen grund haben
}

export interface AenderungAuswahlDTO {
  gewaehlterRaum: RaumAlternativeDTO | null;
  gewaehlteLehrkraft: LehrkraftAlternativeDTO | null;
}

export interface AenderungsVorschlagDTO {
  raumalternativen: RaumAlternativeDTO[];
  lehrkraftalternativen: LehrkraftAlternativeDTO[];
}

export interface OffeneAenderungDTO {
  id: number;
  typ: EreignisTyp;
  titel: string;
  beschreibung: string;
  ereignisId: number;
  unterrichtsstundeId: number;
  klassenstufeId: number;
  klasseId: number;
  erstelltAm: LocalDateTime;
}

export interface LehrkraftAlternativeDTO {
  vorname: string;
  nachname: string;
  lehrkraftId: number;
  fachlehrkraft: boolean;
}

export interface RaumAlternativeDTO {
  bezeichnung: string;
  raumId: number;
  fachraum: boolean;
}

export interface ZeitslotDTO {
  id: number;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
}


@Injectable({ providedIn: 'root' })
export class VertretungsplanService {
  //Anfragen ans Backend und URL festlegen
  private http = inject(HttpClient);
  private startUrl = `${environment.apiUrl}/lerngruppen`; 

  private stundenplan = signal<UnterrichtsstundeDTO[]>([]);
  public readonly alleStundenplaene = this.stundenplan.asReadonly();

  private aenderungsVorschlag = signal<AenderungsVorschlagDTO | null>(null);
  public readonly aktuellerAenderungsVorschlag = this.aenderungsVorschlag.asReadonly();

  private offeneAenderung = signal<OffeneAenderungDTO | null>(null);
  private readonly aktuelleOffeneAenderung = this.offeneAenderung.asReadonly();

  private zeitslots = signal<ZeitslotDTO[]>([]);
  public readonly alleZeitslots = this.zeitslots.asReadonly();

  //Laden
  stundenplanLaden(klassenstufeId: number, lerngruppeId: number) {
    this.stundenplan.set([]); //stundenplan leeren, damit während der neue Stundenplan lädt nciht der von der vorher besuchten klasse angezeigt wird
    this.http.get<UnterrichtsstundeDTO[]>(`${this.startUrl}/${klassenstufeId}/${lerngruppeId}`).subscribe({
        next: (daten) => this.stundenplan.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  offeneAenderungLaden(id: number) {
    this.http.get<OffeneAenderungDTO>(`${environment.apiUrl}/aenderungsverlauf/offen/${id}`).subscribe({
      next: daten => this.offeneAenderung.set(daten),
      error: err => console.error('Fehler beim Laden der offenen Änderung:', err)
    });
  }

  aenderungsVorschlageLaden(klassenstufeId: number, lerngruppeId: number, aenderungId: number) {
    this.http.get<AenderungsVorschlagDTO>(`${this.startUrl}/${klassenstufeId}/${lerngruppeId}/aenderungen/${aenderungId}/vorschlaege`).subscribe({
        next: (daten) => this.aenderungsVorschlag.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  zeitslotsLaden() {
    this.http.get<ZeitslotDTO[]>(`${environment.apiUrl}/dashboard/zeitslots`).subscribe({
      next: (daten) => { this.zeitslots.set(daten); },
      error: (err) => { console.error('Fehler beim Laden der Zeitslots:', err); }
    });
  }

  //Änderungen
  aenderungBestaetigen(
    klassenstufeId: number,
    lerngruppeId: number,
    aenderungId: number,
    auswahl: AenderungAuswahlDTO
  ): Observable<UnterrichtsstundeDTO> {

    return this.http
    .put<UnterrichtsstundeDTO>(
      `${this.startUrl}/${klassenstufeId}/${lerngruppeId}/aenderungen/${aenderungId}/bestaetigen`,
      auswahl
    )
    .pipe(
      tap(() => {
        this.stundenplanLaden(klassenstufeId, lerngruppeId);
        this.aenderungsVorschlag.set(null);
      }),
      catchError((err) => {
        console.error('Fehler beim Bestätigen der Änderung:', err);
        throw err;
      })
    );
  }

  aenderungAlsErledigtMarkieren(id: number) {
    return this.http.put<void>(`${environment.apiUrl}/aenderungsverlauf/offen/${id}/erledigt`, {});
  }

}