//Imports
import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export type LocalDate = string;
export type LocalTime = string;

//Enums
export enum VerfuegbarkeitsTyp {
  BEVORZUGT = 'BEVORZUGT',
  NEUTRAL = 'NEUTRAL',
  NICHT_BEVORZUGT = 'NICHT_BEVORZUGT',
  GESPERRT = 'GESPERRT'
}
export enum Anstellungsverhaeltnis {
  VOLLZEIT = 'VOLLZEIT',
  TEILZEIT = 'TEILZEIT'
}

export enum Wochentag {
  MONTAG = 'MONTAG',
  DIENSTAG = 'DIENSTAG',
  MITTWOCH = 'MITTWOCH',
  DONNERSTAG = 'DONNERSTAG',
  FREITAG = 'FREITAG'
}


//DTOs
export interface VerfuegbarkeitFuerLehrkraftDTO {
  zeitslotId: number | null; //null, weil bei der Eintragung der Verfügbarkeit noch keine ID vorliegt, die wird vom Backend generiert
  stundennummer: number;
  wochentag: Wochentag;
  typ: VerfuegbarkeitsTyp;
  ganztaegig: boolean;
  startdatum: LocalDate;
  enddatum: LocalDate;
}

export interface VerfuegbarkeitEingabeDTO {
  zeitslotId: number | null; //null, weil bei der Eintragung der Verfügbarkeit noch keine ID vorliegt, die wird vom Backend generiert
  wochentag: Wochentag;
  typ: VerfuegbarkeitsTyp;
  ganztaegig: boolean;
  startdatum: LocalDate;
  enddatum: LocalDate;
}

export interface ZeitslotDTO {
  id: number;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
}

//Klassenleitung einer Lehrkraft
export interface LerngruppeFuerLehrkraftDTO {
  bezeichnung: string;
  jahrgangsstufe: number;
  klassenzimmer: string;
}

export interface Praeferenz {
  id: number;
  bezeichnung: string;
  jahrgangsnummer: number;
}

export interface LehrkraftAnlegenDTO {
  name: string;
  vorname: string;
  passwort: string;

  anstellungsverhaeltnis: Anstellungsverhaeltnis;
  deputatsstunden: number;
  fachIds: number[];

  praeferenzIds: number[];

  lerngruppeId: number | null;
  verfuegbarkeiten: VerfuegbarkeitEingabeDTO[];
}

export interface LehrkraftAnzeigenDTO {
  id: number;
  name: string;
  vorname: string;
  benutzername: string;
  aktiv: boolean;

  kuerzel: string;
  anstellungsverhaeltnis: Anstellungsverhaeltnis;
  deputatsstunden: number;
  faecher: FachUebersichtDTO[];

  praeferenzen: Praeferenz[];

  lerngruppe: LerngruppeFuerLehrkraftDTO;
  verfuegbarkeiten: VerfuegbarkeitFuerLehrkraftDTO[];
}

export interface LehrkraftBearbeitenDTO {
  name: string;
  vorname: string;
  aktiv: boolean;

  anstellungsverhaeltnis: Anstellungsverhaeltnis;
  deputatsstunden: number;
  fachIds: number[];

  praeferenzIds: number[];

  lerngruppeId: number | null;
  verfuegbarkeiten: VerfuegbarkeitEingabeDTO[];
}


export interface FachUebersichtDTO {
  id: number;
  kuerzel: string;
  name: string
}


export type Lehrkraft = LehrkraftAnzeigenDTO;

@Injectable({ providedIn: 'root' })
export class TeacherService {

  //HTTP-Client injizieren, um HTTP-Anfragen ans Backend zu schicken
  //vollständige API-URL zusammenbauen
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/lehrkraefte`;

  //Variablen festlegen
  private lehrkraft = signal<LehrkraftAnzeigenDTO[]>([]);
  public readonly alleLehrkraefte = this.lehrkraft.asReadonly();

  private deaktivierteLehrkraft = signal<LehrkraftAnzeigenDTO[]>([]);
  public readonly alleDaktivierten = this.deaktivierteLehrkraft.asReadonly();

  private faecher = signal<FachUebersichtDTO[]>([]);
  public readonly alleFaecher = this.faecher.asReadonly();

  private praeferenz = signal<Praeferenz[]>([]);
  public readonly allePraeferenzen = this.praeferenz.asReadonly();

  //Hinzufügen + Anlegen
  lehrkraftHinzufuegen(dto: LehrkraftAnlegenDTO) {
      this.http.post<LehrkraftAnzeigenDTO>(this.baseUrl, dto).subscribe({
          next: (neueLehrkraft) => {
              this.lehrkraft.update((liste) => [...liste, neueLehrkraft]);
          },
          error: (err) => console.error('Fehler beim Anlegen:', err),
      });
  }

  //Aktualisieren
  lehrkraftAktualisieren(id: number, dto: LehrkraftBearbeitenDTO) {
    this.http.put<LehrkraftAnzeigenDTO>(`${this.baseUrl}/${id}`, dto).subscribe({
        next: (aktualisiert) => {
            this.lehrkraft.update((liste) =>
                liste.map((l) => (l.id === aktualisiert.id ? aktualisiert : l))
            );
        }, 
        error: (err) => console.error('Fehler beim Aktualisieren', err),
    });
  }

  //Laden
  lehrkraftLaden() {
      this.http.get<LehrkraftAnzeigenDTO[]>(this.baseUrl).subscribe({
          next: (daten) => this.lehrkraft.set(daten),
          error: (err) => console.error('Fehler beim Laden:', err),  
      });
  }

  deaktivierterLehrkraefteLaden() {
      this.http.get<LehrkraftAnzeigenDTO[]>(`${this.baseUrl}/deaktiviert`).subscribe({
          next: (daten) => this.deaktivierteLehrkraft.set(daten),
          error: (err) => console.error('Fehler beim Laden:', err),  
      });
  }

  faecherLaden() {
    this.http.get<FachUebersichtDTO[]>(`${this.baseUrl}/faecher`).subscribe({
        next: (daten) => this.faecher.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),
    })
  }

  praeferenzLaden() {
    this.http.get<Praeferenz[]>(`${this.baseUrl}/klassen`).subscribe({
      next: (daten) => this.praeferenz.set(daten),
      error: (err) => console.error('Fehler beim Laden:', err),
    })
  }

  //Aktivieren + Deaktivieren
  lehrkraftAktivieren(lehrkraft: LehrkraftAnzeigenDTO) {
    const dto: LehrkraftBearbeitenDTO = {
      name: lehrkraft.name,
      vorname: lehrkraft.vorname,
      aktiv: true,
      anstellungsverhaeltnis: lehrkraft.anstellungsverhaeltnis,
      deputatsstunden: lehrkraft.deputatsstunden,
      fachIds: lehrkraft.faecher.map(f => f.id),
      praeferenzIds: lehrkraft.praeferenzen.map(p => p.id),
      lerngruppeId: null, 
      verfuegbarkeiten: lehrkraft.verfuegbarkeiten.map(v => ({
        zeitslotId: v.zeitslotId,
        wochentag: v.wochentag,
        typ: v.typ,
        startdatum: v.startdatum,
        enddatum: v.startdatum
      } as VerfuegbarkeitEingabeDTO))
    };
    this.http.put<LehrkraftAnzeigenDTO>(`${this.baseUrl}/${lehrkraft.id}`, dto).subscribe({
      next: (aktualisiert) => {
        this.deaktivierteLehrkraft.update((liste) => liste.filter((x) => x.id !== lehrkraft.id));
        this.lehrkraft.update((liste) => liste.some(x => x.id === aktualisiert.id) ? liste : [...liste, aktualisiert]);
      },
      error: (err) => console.error('Fehler beim Aktivieren:', err),
    });
  }

  lehrkraftDeaktivieren(lehrkraft: LehrkraftAnzeigenDTO) {
    this.http.put<void>(`${this.baseUrl}/${lehrkraft.id}/deaktivieren`, null).subscribe({
      next: () => {
        this.lehrkraft.update((liste) => liste.filter((x) => x.id !== lehrkraft.id));
        this.deaktivierteLehrkraft.update((liste) => [...liste, { ...lehrkraft, aktiv: false }]);
      },
      error: (err) => console.error('Fehler beim Deaktivieren:', err),
    });
  }

}