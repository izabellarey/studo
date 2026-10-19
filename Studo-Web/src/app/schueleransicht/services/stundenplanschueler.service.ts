import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

//------------------- Dtos ------------------------------------

export interface SchuelerStundeDTO {
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

//----------------------- Schüleransicht Methoden -----------------------------------------------

@Injectable({ providedIn: 'root' })
export class StundenplanSchuelerService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/schueleransicht`;

    private stundenplan = signal<SchuelerStundeDTO[]>([]);
    public readonly alleStunden = this.stundenplan.asReadonly();

    ladeStundenplan() {
        this.http.get<SchuelerStundeDTO[]>(`${this.baseUrl}/stundenplan`).subscribe({
            next: (daten) => this.stundenplan.set(daten),
            error: (err) => console.error('Fehler beim Laden des Stundenplans:', err),
        });
    }
}