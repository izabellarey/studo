import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { environment } from '../../../environments/environment';

//---------------------------------------- DTOs --------------------------------------------

export enum Wochentag {
    MONTAG = 'MONTAG',
    DIENSTAG = 'DIENSTAG',
    MITTWOCH = 'MITTWOCH',
    DONNERSTAG = 'DONNERSTAG',
    FREITAG = 'FREITAG',    
}


export enum PlanStatus {
    ENTWURF = 'ENTWURF',
    GEWAEHLT = 'GEWAEHLT',
    VEROEFFENTLICHT = 'VEROEFFENTLICHT'
}

export type GesamtvarianteNummer = 1 | 2 | 3;

export interface KlasseUebersichtDTO {
    id: number;
    bezeichnung: string;
    jahrgangsnummer: number;
}

export interface UnterrichtsstundeDTO{
    id: number;
    wochentag: Wochentag;
    stundennummer: number;
    start: string;  //LocalTime
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

export interface StundenplanAnzeigenDTO {
    id: number;
    status: PlanStatus;
    variante: GesamtvarianteNummer;
    unterrichtsstunden: UnterrichtsstundeDTO[];
}

export interface RaumAlternativeDTO {
    raumId: number;
    bezeichnung: string;
    fachraum: boolean;
}

export interface LehrkraftAlternativeDTO {
    lehrkraftId: number;
    vorname: string;
    nachname: string;
    fachlehrkraft: boolean;
}

export interface AenderungsVorschlagDTO {
    raumalternativen: RaumAlternativeDTO[];
    lehrkraftalternativen: LehrkraftAlternativeDTO[];
}

export interface UnterrichtsstundeAendernDTO {
    lehrkraftId: number | null;
    raumId: number | null;
}

export interface GenerationDTO {
    generationId: number;
}

export interface GenerierungStatusDTO {
    status: 'LAEUFT' | 'FERTIG' | 'FEHLER';
}

export interface GenerierungGestartetDTO {
    generationId: number;
}


//-------------------------------------------------- Service --------------------------

@Injectable({ providedIn: 'root'})
export class InitstundenplanService {
    private http = inject(HttpClient);
    private readonly baseUrl = environment.apiUrl;

   getKlassen(): Observable<KlasseUebersichtDTO[]> {
    return this.http.get<KlasseUebersichtDTO[]>(`${this.baseUrl}/stundenplaene/klassen`);
   }

   getAktuelleGeneration(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/stundenplaene/aktuelle-generation`);
   }

   generierungStarten(): Observable<GenerierungGestartetDTO> {
    return this.http.post<GenerierungGestartetDTO>(`${this.baseUrl}/stundenplaene/generieren`, {});
   }

   getGenerierungStatus(generationId: number): Observable<GenerierungStatusDTO> {
    return this.http.get<GenerierungStatusDTO>(`${this.baseUrl}/stundenplaene/generierung-status/${generationId}`);
   }

   getStundenplanVorschlaege(klasseId: number): Observable<StundenplanAnzeigenDTO[]> {
    return this.http.get<StundenplanAnzeigenDTO[]>(`${this.baseUrl}/klassen/${klasseId}/stundenplan-vorschlaege`);
   }

   getStundenplanFuerVariante(
    klasseId: number,
    variante: GesamtvarianteNummer,
    generationId: number
   ): Observable<StundenplanAnzeigenDTO> {
    return this.http.get<StundenplanAnzeigenDTO>(`${this.baseUrl}/stundenplaene/variante/${klasseId}`,
        {params: {generationId, variante}}
    );
   }

   veroeffentlichen(generationId: number, variante: GesamtvarianteNummer): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/stundenplaene/veroeffentlichen`, {},
        {params: {generationId, variante}}
    );
   }

   getAenderungsVorschlag(unterrichtsstundeId: number): Observable<AenderungsVorschlagDTO> {
    return this.http.get<AenderungsVorschlagDTO>(`${this.baseUrl}/stundenplaene/unterrichtsstunde/${unterrichtsstundeId}/aenderung`);
   }
   
   stundeAendern(unterrichtsstundeId: number, dto: UnterrichtsstundeAendernDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/stundenplaene/unterrichtsstunde/${unterrichtsstundeId}/aenderung`, dto);
   }

}
