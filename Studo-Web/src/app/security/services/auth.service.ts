import { Injectable, inject, signal, computed } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

//------------------------------- DTOs --------------------------------

export type Rolle = 'PLANUNG' | 'LEHRKRAFT' | 'SCHUELER';

export interface LoginRequest {
    benutzername: string;
    passwort: string;
}

export interface LoginResponse {
    token: string;
    name: string;
    vorname: string;
    rolle: Rolle;
}

export interface AuthUser {
    name: string;
    vorname: string;
    rolle: Rolle;
}

const TOKEN_KEY = 'studo_auth_token';
const USER_KEY = 'studo_auth_user';

export function startseiteFuerRolle(rolle: Rolle): string {
    switch (rolle) {
        case 'PLANUNG':
            return '/planungsansicht/dashboard';
        case 'LEHRKRAFT':
            return '/lehreransicht';
        case 'SCHUELER':
            return '/schueleransicht';
    }
}


//-------------------- RestAPI_Handler und Authentifizierungsmanagement ------------------------------------------
//grade keine Rolle und nur provisorisch mit query-param -> izabella fragen wegen token

@Injectable({ providedIn: 'root'})
export class AuthService {
    private http = inject(HttpClient);
    private router = inject(Router);
    private baseUrl = environment.apiUrl;

    private currentUserSignal = signal<AuthUser | null>(this.loadStoredUser());
    readonly currentUser = this.currentUserSignal.asReadonly();
    readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

    login(credentials: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, credentials).pipe(tap((response) => this.persistSession(response)));
    }
    
    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        this.currentUserSignal.set(null);
        this.router.navigateByUrl('/login', {replaceUrl: true});
    }

    getToken(): string | null {
        return localStorage.getItem(TOKEN_KEY);
    }

    private persistSession(response: LoginResponse): void {
        const user: AuthUser = {name: response.name, vorname: response.vorname, rolle: response.rolle};
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUserSignal.set(user);
    }

    private loadStoredUser(): AuthUser | null {
        const raw = localStorage.getItem(USER_KEY);
        return raw ? (JSON.parse(raw) as AuthUser) : null;
    }
}