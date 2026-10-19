import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

//------------------- Dtos ------------------------------------
export interface SchuelerAnzeigenDto {
    id: number;
    name: string;
    vorname: string;
    benutzername: string;
    klasse: string;
    klassenraum: string;
    aktiv: boolean;
    klassenId: number;
    eintrittsdatum: string;  //ISO Jahr Monat Tag
    austrittsdatum: string | null;
}


export interface SchuelerAnlegenDto {
    name: string;
    vorname: string;
    passwort: string;
    eintrittsdatum: string;
    klassenId: number;
}

export interface SchuelerBearbeitenDto {
    name: string;
    vorname: string;
    aktiv: boolean;
    austrittsdatum: string | null;
    klassenId: number;   
}

export interface PasswortZuruecksetzenDto {
    neuesPasswort: string;
}

export interface LerngruppeUebersichtDTO {
    id: number;
    bezeichnung: string;
    jahrgangsnummer: number;
}


//----------------------- Schüler Methoden -----------------------------------------------


@Injectable({ providedIn: 'root'})
export class SchuelerService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/schueler`; 

    private schueler = signal<SchuelerAnzeigenDto[]>([]);
    public readonly alleSchueler = this.schueler.asReadonly();

    private deaktivierteSchueler = signal<SchuelerAnzeigenDto[]>([]);
    public readonly alleDaktivierten = this.deaktivierteSchueler.asReadonly();

    ladeSchueler() {
        this.http.get<SchuelerAnzeigenDto[]>(this.baseUrl).subscribe({
            next: (daten) => this.schueler.set(daten),
            error: (err) => console.error('Fehler beim Laden der Schüler:', err),  
        });
    }

    ladeDeaktivierteSchueler() {
        this.http.get<SchuelerAnzeigenDto[]>(`${this.baseUrl}/deaktiviert`).subscribe({
            next: (daten) => this.deaktivierteSchueler.set(daten),
            error: (err) => console.error('Fehler beim Laden der deaktivierten Schüler:', err),  
        });
    }

    schuelerHinzufuegen(dto: SchuelerAnlegenDto) {
        this.http.post<SchuelerAnzeigenDto>(this.baseUrl, dto).subscribe({
            next: (neuerSchueler) => {
                this.schueler.update((liste) => [...liste, neuerSchueler]);
            },
            error: (err) => console.error('Fehler beim Anlegen des Schülers:', err),
        });
    }

    schuelerAktualisieren(id: number, dto: SchuelerBearbeitenDto) {
        this.http.put<SchuelerAnzeigenDto>(`${this.baseUrl}/${id}`, dto).subscribe({
            next: (aktualisiert) => {
                this.schueler.update((liste) =>
                    liste.map((s) => (s.id === aktualisiert.id ? aktualisiert : s))
                );
            }, 
            error: (err) => console.error('Fehler beim Aktualisieren des Schülers', err),
        });
    }


    //---------------------- Status ---------------------------------------------
    
    
    schuelerDeaktivieren(s: SchuelerAnzeigenDto) {
        this.http.put<void>(`${this.baseUrl}/${s.id}/deaktivieren`, {}).subscribe({
            next: () => {
                this.schueler.update((liste) => liste.filter((x) => x.id !== s.id));
                this.deaktivierteSchueler.update((liste) => [...liste, {...s, aktiv: false}]);
            },
            error: (err) => console.error('Fehler beim Deaktivieren:', err),
        });
    }

    schuelerReaktivieren(s: SchuelerAnzeigenDto) {
        const dto: SchuelerBearbeitenDto = {
            name: s.name,
            vorname: s.vorname,
            aktiv: true,
            austrittsdatum: null,
            klassenId: s.klassenId,
        };
        this.http.put<SchuelerAnzeigenDto>(`${this.baseUrl}/${s.id}`, dto).subscribe({
            next: (aktualisiert) => {
                this.deaktivierteSchueler.update((liste) => liste.filter((x) => x.id !== s.id));
                this.schueler.update((liste) => [...liste, aktualisiert]);
            },
            error: (err) => console.error('Fehler beim Reaktivieren:', err),
        });
    }


    //----------------- Zurücksetzen, ohne altes Passwort ---------------------------------------
    

    passwortZuruecksetzen(id: number, neuesPasswort: string) {
        const dto: PasswortZuruecksetzenDto = { neuesPasswort };
        this.http.patch<void>(`${this.baseUrl}/${id}/passwort`, dto).subscribe(); //Hier wieder richtige Route später anlegen
    }


    //---------------- Klassenauswahl beim Anlegen ---------------------------------------


    private klassen = signal<LerngruppeUebersichtDTO[]>([]);
    public readonly alleKlassen = this.klassen.asReadonly();

    ladeKlassen() {
        this.http.get<LerngruppeUebersichtDTO[]>(`${this.baseUrl}/klassen`).subscribe({
            next: (daten) => this.klassen.set(daten),
            error: (err) => console.error('Fehler beim Laden der Klassen:', err),
        });
    }
}

