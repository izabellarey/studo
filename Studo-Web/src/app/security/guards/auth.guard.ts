import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

//Schützt die Ansichten, wenn nicht eingeloggt -> Nutzer werden zur Login Route weitergeleitet 
export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (auth.isAuthenticated()) {
        return true;
    }
    
    router.navigateByUrl('/login', {replaceUrl: true});
    return false;
};