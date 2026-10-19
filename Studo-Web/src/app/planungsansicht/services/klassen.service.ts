import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface JahrgangsstufeDto {
  id: number;
  nummer: number;
  anzahlKlassen: number;
}

export interface KlasseAnlegenDto {
  bezeichnung: string;
}

export interface LerngruppeBearbeiten {
  neueBezeichnung: string;
}

export interface LerngruppeDto {
  lerngruppeId: number;
  bezeichnung: string;
  lehrkraftVorname: string;
  lehrkraftNachname: string;
  schueleranzahl: number;
  klassenzimmer: string;
}

export interface LerngruppeUebersichtDTO {
  id: number;
  bezeichnung: string;
  jahrgangsnummer: number;
}


@Injectable({ providedIn: 'root'})
export class LerngruppeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/lerngruppen`;

  readonly jahrgangsstufen = signal<JahrgangsstufeDto[]>([]);
  readonly klassen = signal<LerngruppeDto[]>([]);
 
  // GET /api/lerngruppen  (Nr. 5) -> alle Jahrgangsstufen
  ladeJahrgangsstufen(): void {
    this.http.get<JahrgangsstufeDto[]>(this.baseUrl).subscribe({
      next: (liste) => this.jahrgangsstufen.set(liste),
      error: (err) => console.error('Fehler beim Laden der Jahrgangsstufen:', err),
    });
  }
  
  //Fragt nach Liste der Klassen aus dem backend
  ladeKlassen(jahrgangsstufeId: number): void {
    this.http.get<LerngruppeDto[]>(`${this.baseUrl}/${jahrgangsstufeId}`).subscribe({
      next: (liste) => this.klassen.set(liste),
      error: (err) => console.error('Fehler beim Laden der Klassen:', err),
   });
  }

  // Klasse erstellen
  erstelleKlasse(jahrgangsstufeId: number, daten: KlasseAnlegenDto): void {
    this.http.post<LerngruppeDto>(`${this.baseUrl}/${jahrgangsstufeId}/klasseAnlegen`, daten).subscribe({
      next: (neu) => this.klassen.update((liste) => [...liste, neu]),
      error: (err) => console.error('Fehler beim Anlegen:', err),
    });
  }

    bearbeiteKlasse(lerngruppeId: number, daten: LerngruppeBearbeiten): void {
    this.http.put<LerngruppeDto>(`${this.baseUrl}/${lerngruppeId}/bearbeiten`, daten).subscribe({
        next: (neu) => this.klassen.update((liste) => liste.map((k) => (k.lerngruppeId === neu.lerngruppeId ? neu : k)),),
        error: (err) => console.error('Fehler beim Bearbeiten:', err),
    });
  }

    // PUT /api/lerngruppen/{lerngruppeId}/deaktivieren  (Nr. 11)
  deaktiviereKlasse(k: LerngruppeDto): void {
    this.http.put<void>(`${this.baseUrl}/${k.lerngruppeId}/deaktivieren`, {}).subscribe({
      next: () => this.klassen.update((liste) => liste.filter((x) => x.lerngruppeId !== k.lerngruppeId)),
      error: (err) => console.error('Fehler beim Deaktivieren:', err),
    });
  }
}
