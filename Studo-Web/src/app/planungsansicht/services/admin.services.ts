//Imports
import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

//DTOs
export interface AccountAnlegenDTO{
  name: string;
  vorname: string;
  passwort: string;
}

export interface AccountAnzeigenDTO {
  id: number;
  name: string;
  vorname: string;
  benutzername: string;
  aktiv: boolean;
}

export interface AccountBearbeitenDTO {
  name: string;
  vorname: string;
  aktiv: boolean;
}

export type Admin = AccountAnzeigenDTO;

@Injectable({ providedIn: 'root' })
export class AdminService {

  //HTTP-Client injizieren, um HTTP-Anfragen ans Backend zu schicken
  //vollständige API-URL zusammenbauen
  private http = inject(HttpClient); 
  private startUrl = `${environment.apiUrl}/accounts`; 

  //signal ist ein "Container" für einen Wert, der sich ändert und automatisch
  //überall aktualisiert wird
  private admin = signal<AccountAnzeigenDTO[]>([]);
  //als readonly, damit die Werte nicht ohne die ServiceDatei änderbar sind
  public readonly alleAdmins = this.admin.asReadonly();

  private deaktivierterAdmin = signal<AccountAnzeigenDTO[]>([]);
  public readonly alleDaktivierten = this.deaktivierterAdmin.asReadonly();

  //POST-Request mit einem AccountAnlegenDTO-Objekt an das Backend
  adminHinzufuegen(dto: AccountAnlegenDTO) {
    //Backend antwortet mit einem AccountAnzeigenDTO - ein neu angelegter Admin
    this.http.post<AccountAnzeigenDTO>(this.startUrl, dto).subscribe({
        //bei Erfolg wird ein neuer Admin angelegt und die liste der Admins geupdated
        next: (neuerAdmin) => {
            this.admin.update((liste) => [...liste, neuerAdmin]);
        },
        //sonst wird der Fehler in der Konsole ausgegeben
        error: (err) => console.error('Fehler beim Anlegen', err),
    });
  }

  adminLaden() {
    //verbindung zum backend
    this.http.get<AccountAnzeigenDTO[]>(this.startUrl).subscribe({
        next: (daten) => this.admin.set(daten),
        error: (err) => console.error('Fehler beim Laden:', err),  
    });
  }

  //deaktivierte Admins laden 
  deaktivierteAdminsLaden() {
      this.http.get<AccountAnzeigenDTO[]>(`${this.startUrl}/deaktiviert`).subscribe({
          next: (daten) => this.deaktivierterAdmin.set(daten),
          error: (err) => console.error('Fehler beim Laden:', err),  
      });
  }

  //Admin Deaktivieren PUT /api/accounts/{id}/deaktivieren
  adminDeaktivieren(admin: AccountAnzeigenDTO) {
      this.http.put<void>(`${this.startUrl}/${admin.id}/deaktivieren`, null).subscribe({
          next: () => {
              this.admin.update((liste) => liste.filter((x) => x.id !== admin.id));
              this.deaktivierterAdmin.update((liste) => [...liste, { ...admin, aktiv:false}])
          },
          error: (err) => console.error('Fehler beim Deaktivieren:', err),
      });
  }

  //Admin Aktivieren PUT /api/accounts/{id}
  adminAktivieren(admin: AccountAnzeigenDTO) {
      const dto: AccountBearbeitenDTO = {
          name: admin.name,
          vorname: admin.vorname,
          aktiv: true,
      };
      this.http.put<AccountAnzeigenDTO>(`${this.startUrl}/${admin.id}`, dto).subscribe({
          next: (aktualisiert) => {
              this.deaktivierterAdmin.update((liste) => liste.filter((x) => x.id !== admin.id));
              this.admin.update((liste) => liste.some(x => x.id === aktualisiert.id) ? liste : [...liste, aktualisiert]);
          },
          error: (err) => console.error('Fehler beim Aktivieren:', err),
      });
  }

  //Admin Aktualisieren PUT /api/accounts/{id}
  adminAktualisieren(id: number, dto: AccountBearbeitenDTO) {
      this.http.put<AccountAnzeigenDTO>(`${this.startUrl}/${id}`, dto).subscribe({
          next: (aktualisiert) => {
              this.admin.update((liste) =>
                  liste.map((admin) => (admin.id === aktualisiert.id ? aktualisiert : admin))
              );
          }, 
          error: (err) => console.error('Fehler beim Aktualisieren', err),
      });
  }
  

}