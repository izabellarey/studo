//Import
import { Injectable, signal, inject, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export type LocalTime = string;

//Enum
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

export enum Bundesland {
  SCHLESWIG_HOLSTEIN = 'SCHLESWIG_HOLSTEIN'
}

export enum Schulform {
  GYMNASIUM = 'GYMNASIUM'
}

//DTOs
export interface StammdatenDTO {
  schulprofil: SchulprofilDTO;
  stundenraster: ZeitslotDTO[];
  pausenregeln: PausenregelDTO[];
  pflichtfaecher: PflichtfachvorgabeDTO[];
  vertretungsprioritaet: VertretungsprioritaetDTO[];
}

export interface SchulprofilDTO {
  bundesland: Bundesland;
  schulform: Schulform;
  name: string;
}

export interface ZeitslotDTO {
  id: number;
  stundennummer: number;
  start: LocalTime;
  ende: LocalTime;
}

export interface PausenregelDTO {
  id: number;
  mindestpauseNachStunden: number;
  mindestdauerMinuten: number;
  jahrgangsstufeId: number;
  aktiv: boolean;
}

export interface PflichtfachvorgabeDTO {
  fachId: number;
  fachkuerzel: string;
  fachname: string;
  hauptfach: boolean;
  stufen: StufenvorgabeDTO[];
}

export interface FachUebersichtDTO {
  id: number;
  kuerzel: string;
  name:string;
  benoetigterRaumtyp: Raumart;
}

export interface FachAnlegenDTO {
  kuerzel: string;
  name: string;
  benoetigterRaumtyp: Raumart;
}

export interface VertretungsprioritaetDTO {
  jahrgangsstufeId: number;
  nummer: number;
  prio: number;
}

export interface StundenrasterDTO {
  id: number;
  schulbeginn: LocalTime;
  stundendauer: number; //hier ist die Dauer in minuten gemeint
  stundenProTag: number;
  pausen: RasterpausenDTO[];
  zeitslots: ZeitslotDTO[];
}

export interface StufenvorgabeDTO {
  id: number;
  jahrgangsstufeId: number;
  jahrgangsnummer: number;
  stundenanzahl: number;
  doppelstundenBevorzugt: boolean;
  aktiv: boolean;
}

export interface RasterpausenDTO {
  id: number;
  nachStunde: number;
  dauerMinuten: number;
}

export interface MaximalbelastungDTO {
  id: number;
  maxStundenTag: number;
  maxStundenWoche: number;
  jahrgangsstufeId: number;
  aktiv: boolean;
}

export interface BlockregelDTO{
  id: number | null;
  fachId: number;
  jahrgangsstufeId: number;
  aktiv: boolean;
}

@Injectable({ providedIn: 'root' })
export class StammdatenService {

  //HTTP-Client injizieren, um HTTP-Anfragen ans Backend zu schicken
  private http = inject(HttpClient);
  private startUrl = `${environment.apiUrl}/stammdaten`; 

  //Variablen deklarieren
  private stammdaten = signal<StammdatenDTO | null>(null);
  public readonly alleStammdaten = this.stammdaten.asReadonly();

  private schulprofil = signal<SchulprofilDTO | null>(null);
  public readonly schulprofilSignal = this.schulprofil.asReadonly();

  private pausenregel = signal<PausenregelDTO[]>([]);
  public readonly allePausenregeln = this.pausenregel.asReadonly();

  private pflichtfachvorgabe = signal<PflichtfachvorgabeDTO[]>([]);
  public readonly allePflichtfachvorgaben = this.pflichtfachvorgabe.asReadonly();

  private faecher = signal<FachUebersichtDTO[]>([]);
  public readonly alleFaecher = this.faecher.asReadonly();

  private vertretungsprioritaet = signal<VertretungsprioritaetDTO[]>([]);
  public readonly alleVertretungsprioritaeten = this.vertretungsprioritaet.asReadonly();

  private stundenraster = signal<StundenrasterDTO | null>(null);
  public readonly stundenrasterSignal = this.stundenraster.asReadonly();

  private stufenvorgabe = signal<StufenvorgabeDTO[]>([]);
  public readonly alleStufenvorgaben = this.stufenvorgabe.asReadonly();

  private maximalbelastung = signal<MaximalbelastungDTO[]>([]);
  public readonly gesamteMaximalbelastung = this.maximalbelastung.asReadonly();

  alleFaecherAlsPflichtfachvorgaben = computed<PflichtfachvorgabeDTO[]>(() => {
    const faecher = this.alleFaecher();
    const pflichtfaecher = this.allePflichtfachvorgaben();

    return faecher.map((fach) => {
      const bestehende = pflichtfaecher.find((p) => p.fachId === fach.id);
      return bestehende ?? {
        fachId: fach.id,
        fachkuerzel: fach.kuerzel,
        fachname: fach.name,
        hauptfach: false,
        stufen: []
      };
    });
  });

  //Funktionen - Stammdaten
  stammdatenLaden() {
    this.http.get<StammdatenDTO>(this.startUrl).subscribe({
        next: (daten) => this.stammdaten.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  //Funktionen - Schulprofil
  schulprofilLaden() {
    this.http.get<SchulprofilDTO>(`${this.startUrl}/schulprofil`).subscribe({
        next: (daten) => this.schulprofil.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  schulprofilSpeichern(dto: SchulprofilDTO) {
      this.http.put<SchulprofilDTO>(`${this.startUrl}/schulprofil`, dto).subscribe({
          next: (daten) => this.schulprofil.set(daten), 
          error: (err) => console.error('Fehler beim Speichern', err),
      });
  }

  //Funktionen - Pausenregel
  pausenregelLaden() {
    this.http.get<PausenregelDTO[]>(`${this.startUrl}/pausenregeln`).subscribe({
        next: (daten) => this.pausenregel.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  //Funktionen - Pflichtfachvorgaben
  pflichtvorgabenLaden() {
    this.http.get<PflichtfachvorgabeDTO[]>(`${this.startUrl}/pflichtfaecher`).subscribe({
        next: (daten) => this.pflichtfachvorgabe.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  stufenvorgabenHinzufuegen(fach: PflichtfachvorgabeDTO, dto: StufenvorgabeDTO) {
    if (fach.hauptfach) {
      console.warn('Stufenvorgaben für Hauptfächer können nicht angelegt werden.');
      return;
    }
    this.http.post<StufenvorgabeDTO>(`${this.startUrl}/pflichtfaecher/${fach.fachId}/stufen`, dto).subscribe({
      next: (neueStufe) =>
        this.pflichtfachvorgabe.update((liste) =>
          liste.map((f) => (f.fachId === fach.fachId ? { ...f, stufen: [...f.stufen, neueStufe] } : f))
        ),
      error: (err) => console.error('Fehler beim Anlegen:', err),
    });
  }

  stufenvorgabenAktualisieren(fachId: number, stufeId: number, dto: StufenvorgabeDTO) {
    this.http.put<StufenvorgabeDTO>(`${this.startUrl}/pflichtfaecher/${fachId}/stufen/${stufeId}`, dto).subscribe({
      next: (aktualisiert) =>
        this.pflichtfachvorgabe.update((liste) =>
          liste.map((f) =>
            f.fachId === fachId
              ? { ...f, stufen: f.stufen.map((s) => (s.id === aktualisiert.id ? aktualisiert : s)) }
              : f
          )
        ),
      error: (err) => console.error('Fehler beim Aktualisieren', err),
    });
  }

  stufenvorgabenLoeschen(fach: PflichtfachvorgabeDTO, stufeId: number) {
    if (fach.hauptfach) {
      //Hauptfächer können nicht gelöscht werden daher
      return;
    }
    this.http.delete(`${this.startUrl}/pflichtfaecher/${fach.fachId}/stufen/${stufeId}`).subscribe({
      next: () => this.pflichtfachvorgabe.update((liste) => liste.map((f) => f.fachId === fach.fachId ? { ...f, stufen: f.stufen.filter((s) => s.id !== stufeId) } : f)),
      error: (err) => console.error('Fehler beim Löschen', err),
    });

  }

  //Fach hinzufügen
  faecherLaden() {
    this.http.get<FachUebersichtDTO[]>(
      `${this.startUrl}/faecher`
    ).subscribe({
      next: (daten) => this.faecher.set(daten),
      error: (err) => console.error('Fehler beim Laden der Fächer:', err)
    });
  }

  fachHinzufuegen(dto: FachAnlegenDTO) {
    this.http.post<FachUebersichtDTO>(`${this.startUrl}/faecher`, dto).subscribe({
      next: (neuesFach) => {
        this.faecher.update((liste) => [...liste, neuesFach]);
      },
      error: (err) => console.error('Fehler beim Anlegen des Fachs:', err)
    });
  }

  //Funktionen - Vertretungsprioritaeten
  vertretungsprioritaetenLaden() {
    this.http.get<VertretungsprioritaetDTO[]>(`${this.startUrl}/vertretungsprioritaet`).subscribe({
        next: (daten) => this.vertretungsprioritaet.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  vertretungsprioritaetenSpeichern(reihenfolge: VertretungsprioritaetDTO[]) {
    this.http.put<VertretungsprioritaetDTO[]>(`${this.startUrl}/vertretungsprioritaet`, reihenfolge).subscribe({
      next: (daten) => this.vertretungsprioritaet.set(daten),
      error: (err) => console.error('Fehler beim Speichern:', err),
    });
  }

  //Funktionen - Stundenraster
  stundenrasterLaden() {
    this.http.get<StundenrasterDTO>(`${this.startUrl}/stundenraster`).subscribe({
        next: (daten) => this.stundenraster.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  stundenrasterpeichern(dto: StundenrasterDTO) {
      this.http.put<StundenrasterDTO>(`${this.startUrl}/stundenraster`, dto).subscribe({
          next: (daten) => this.stundenraster.set(daten), 
          error: (err) => console.error('Fehler beim Speichern', err),
      });
  }

  //Funktionen - Maximalbelastung
  maximalbelastungLaden() {
    this.http.get<MaximalbelastungDTO[]>(`${this.startUrl}/maximalbelastungen`).subscribe({
        next: (daten) => this.maximalbelastung.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  maximalbelastungHinzufuegen(dto: MaximalbelastungDTO) {
    this.http.post<MaximalbelastungDTO>(`${this.startUrl}/maximalbelastungen`, dto).subscribe({
        next: (neueMaximalbelastung) => {
            this.maximalbelastung.update((liste) => [...liste, neueMaximalbelastung]);
        },
        error: (err) => console.error('Fehler beim Anlegen:', err),
    });
  }

  maximalbelastungAktualisieren(id: number, dto: MaximalbelastungDTO) {
    this.http.put<MaximalbelastungDTO>(`${this.startUrl}/maximalbelastungen/${id}`, dto).subscribe({
        next: (aktualisiert) => {
            this.maximalbelastung.update((liste) =>
                liste.map((m) => (m.id === aktualisiert.id ? aktualisiert : m))
            );
        }, 
        error: (err) => console.error('Fehler beim Aktualisieren', err),
    });
  }

  maximalbelastungLoeschen(id: number) {
    this.http.delete(`${this.startUrl}/maximalbelastungen/${id}`).subscribe({
        next: () => {
            this.maximalbelastung.update((liste) =>
                liste.filter((m) => m.id !== id)
            );
        },
        error: (err) => console.error('Fehler beim Löschen', err),
    });
  }
}