import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, Rolle, startseiteFuerRolle } from '../services/auth.service';

//Role Guard geht davon aus, dass ein Nutzer bereits eingeloggt ist
// -> wenn Rolle falsch, dann wird zur Startseite redirected, nicht zum login

export function roleGuard(erlaubteRollen: Rolle[]): CanActivateFn {
    return () => {
        const auth = inject(AuthService);
        const router = inject(Router);
        const rolle = auth.currentUser()?.rolle;

        if (rolle && erlaubteRollen.includes(rolle)) {
            return true;
        }

        router.navigateByUrl(rolle ? startseiteFuerRolle(rolle) : '/login', {replaceUrl: true});
        return false;
    }
}