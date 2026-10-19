import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';


//--------------------- DTOs --------------------------------

export interface AccountProfil {
    id: number;
    name: string;
    vorname: string;
    benutzername: string;
}

export interface PasswortAenderung {
    altesPasswort: string;
    neuesPasswort: string
}

//----------- Services --------------------------------------

@Injectable({
    providedIn: 'root'
})

export class AccountService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/accounts`;

    private _profil = signal<AccountProfil | null>(null);
    profil = this._profil.asReadonly();

    vorname = computed(() => this._profil()?.vorname ?? '');
    nachname = computed(() => this._profil()?.name ?? '');

    kuerzel = computed(() => {
        const v = this.vorname();
        const n = this.nachname();
        if (!v || !n) return '';
        return `${v.charAt(0).toUpperCase()}${n.charAt(0).toUpperCase()}`;
    });

    //Laut Url hier noch provisorisch -> nachfragen
    private get accountId(): number {
        const id = localStorage.getItem('accountId');
        return id ? Number(id) : 1;
    }

    ladeProfil(): Observable<AccountProfil> {
        const params = new HttpParams().set('accountId', this.accountId);
        return this.http.get<AccountProfil>(`${this.baseUrl}/profil`, {params}).pipe(tap(profil => this._profil.set(profil)));
    }

    profilSpeichern(vorname: string, nachname: string, benutzername: string): Observable<AccountProfil> {
        const params = new HttpParams().set('accountId', this.accountId);
        const payload = {name: nachname, vorname, benutzername};
        return this.http.put<AccountProfil>(`${this.baseUrl}/profil`, payload, {params}).pipe(tap(profil => this._profil.set(profil)));
    }

    passwortAendern(altesPasswort: string, neuesPasswort: string): Observable<void> {
        const params = new HttpParams().set('accountId', this.accountId);
        const payload: PasswortAenderung = {altesPasswort, neuesPasswort};
        return this.http.put<void>(`${this.baseUrl}/profil/passwort`, payload, {params});
    }
}