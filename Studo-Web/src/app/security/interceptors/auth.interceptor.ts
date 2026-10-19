import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

//Jeder ausgehender Request kriegt einen Bearer-Token angehangen, wenn Nutzer eingeloggt ist
//Login Request ausgenommen, da da noch kein Token existiert
export const authInterceptor: HttpInterceptorFn = (req, next ) => {
    const auth = inject(AuthService);
    const token = auth.getToken();

    if (!token || req.url.includes('/auth/login')) {
        return next(req);
    }

    const authReq = req.clone({
        setHeaders: {Authorization: `Bearer ${token}`},
    });
    return next(authReq);
}