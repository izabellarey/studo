import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
 
//------------------- Dtos ------------------------------------

export interface LehrerStundeDto {
    id: number;
    wochentag: string;        
    stundennummer: number;
    start: string;            
    ende: string;             
    fachName: string;
    raumBezeichnung: string;
    lehrkraftKuerzel: string;
    lerngruppeKuerzel: string;
    geaendert: boolean;
    freistunde: boolean;
    offeneAenderung: boolean;
    grund: string | null;
}
 
export interface LehrerAenderungDto {
    id: number;
    typ: string;              
    titel: string;
    beschreibung: string;
    offen: boolean;
}
 
export interface LehrerRaumDto {
    id: number;
    bezeichnung: string;
    art: string;
    kapazitaet: number;
}
 
export interface LehrerRaumStundeDto {
    wochentag: string;
    stundennummer: number;
    start: string;
    ende: string;
    fachName: string;
    lehrkraftKuerzel: string;
    lerngruppeKuerzel: string;
    geaendert: boolean;
}

export interface LehrerZeitslot {
    stundennummer: number;
    start: string;
    ende: string;
}
 
 
//----------------------- Lehreransicht Methoden -----------------------------------------------
 
 
@Injectable({ providedIn: 'root'})
export class LehreransichtService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/lehrkraftansicht`;

    private zeitslotUrl = `${environment.apiUrl}/dashboard/zeitslots`;

    //stundenraster vom backend durch zugriff auf stammdaten
    private geladeneZeitslots = signal<LehrerZeitslot[] | null>(null);

    //stunden- und raumplan
    public readonly zeitslots = computed(() => this.slotsBestimmen(this.stundenplan()));
    public readonly raumZeitslots = computed(() => this.slotsBestimmen(this.raumplan()));

    private slotsBestimmen(stunden: LehrerZeitslot[]): LehrerZeitslot[] {
        const geladen = this.geladeneZeitslots();
        if (geladen) {
            return [...geladen].sort((a, b) => a.stundennummer - b.stundennummer);
        }

        const slots = new Map<number, LehrerZeitslot>();
        for (const stunde of stunden) {
            if (!slots.has(stunde.stundennummer)) {
                slots.set(stunde.stundennummer, {
                    stundennummer: stunde.stundennummer,
                    start: stunde.start,
                    ende: stunde.ende,
                });
            }
        }
        return [...slots.values()].sort((a, b) => a.stundennummer - b.stundennummer);
    }


private zeitslotsLaden() {
    if (this.geladeneZeitslots()) return; 
    this.http.get<LehrerZeitslot[]>(this.zeitslotUrl).subscribe({
        next: (daten) => this.geladeneZeitslots.set(daten),
        error: (err) => {
            console.warn('Zeitslots konnten nicht geladen werden, nutze Stunden aus dem Plan:', err);
            this.geladeneZeitslots.set(null);
        },
    });
}
 
    private stundenplan = signal<LehrerStundeDto[]>([]);
    public readonly alleStunden = this.stundenplan.asReadonly();
 
    private aenderungen = signal<LehrerAenderungDto[]>([]);
    public readonly alleAenderungen = this.aenderungen.asReadonly();
    public readonly anzahlOffen = computed(() => this.aenderungen().filter((a) => a.offen).length);
 
    private raeume = signal<LehrerRaumDto[]>([]);
    public readonly alleRaeume = this.raeume.asReadonly();
 
    private raumplan = signal<LehrerRaumStundeDto[]>([]);
    public readonly aktuellerRaumplan = this.raumplan.asReadonly();
 
    ladeStundenplan() {
        this.zeitslotsLaden();
        this.http.get<LehrerStundeDto[]>(`${this.baseUrl}/stundenplan`).subscribe({
            next: (daten) => this.stundenplan.set(daten),
            error: (err) => console.error('Fehler beim Laden des Stundenplans:', err),
        });
    }
 
    ladeAenderungen() {
        this.http.get<LehrerAenderungDto[]>(`${this.baseUrl}/aenderungen`).subscribe({
            next: (daten) => this.aenderungen.set(daten),
            error: (err) => console.error('Fehler beim Laden der Änderungen:', err),
        });
    }
 
    ladeRaeume() {
        this.http.get<LehrerRaumDto[]>(`${this.baseUrl}/raeume`).subscribe({
            next: (daten) => this.raeume.set(daten),
            error: (err) => console.error('Fehler beim Laden der Räume:', err),
        });
    }
 
    
    ladeRaumplan(raumId: number) {
        this.zeitslotsLaden();
        this.raumplan.set([]); //alten Plan leeren, damit nicht kurz der vorherige Raum angezeigt wird
        this.http.get<LehrerRaumStundeDto[]>(`${this.baseUrl}/${raumId}`).subscribe({
            next: (daten) => this.raumplan.set(daten),
            error: (err) => console.error('Fehler beim Laden des Raumplans:', err),
        });
    }
}