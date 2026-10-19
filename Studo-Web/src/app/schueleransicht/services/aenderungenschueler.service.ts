import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AenderungDto {
  id: number;
  typ: string;          
  titel: string;
  beschreibung: string;
  offen: boolean;
  freistunde: boolean;  
}

@Injectable({ providedIn: 'root' })
export class AenderungenService {
  private apiUrl = `${environment.apiUrl}/schueleransicht/aenderungen`;

  private http = inject(HttpClient);

  // Lädt die für den eingeloggten Schüler relevanten Änderungen
  getAenderungen(): Observable<AenderungDto[]> {
    return this.http.get<AenderungDto[]>(this.apiUrl);
  }
}