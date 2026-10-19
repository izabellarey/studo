import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { addIcons } from 'ionicons';
import { mailOutline, lockClosedOutline, eyeOutline, eyeOffOutline } from 'ionicons/icons';
import { AuthService, startseiteFuerRolle } from '../security/services/auth.service';
import { IonContent, 
         IonButton,
         IonIcon,
         ToastController,
         LoadingController
        } from '@ionic/angular';

addIcons({
    'mail-outline': mailOutline,
    'lock-closed-outline': lockClosedOutline,
    'eye-outline': eyeOutline,
    'eye-off-outline': eyeOffOutline,
});

@Component({
    selector: 'app-login',
    standalone: true,
    templateUrl: './login.html',
    styleUrls: ['./login.css'],
    imports: [CommonModule, 
              ReactiveFormsModule, 
              IonContent, 
              IonButton, 
              IonIcon
    ],
})
export class LoginPage {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);
    private toastCtrl = inject(ToastController);
    private loadingCtrl = inject(LoadingController);

    isSubmitting = false;
    showPassword = false;
    currentYear = new Date().getFullYear();

    form = this.fb.nonNullable.group({
        benutzername: ['', [Validators.required]],
        passwort: ['', [Validators.required, Validators.minLength(6)]],
    });

    async onSubmit(): Promise<void> {
        if (this. form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const loading = await this.loadingCtrl.create({message: 'Anmeldung läuft...'});
        await loading.present();
        this.isSubmitting = true;

        this.auth.login(this.form.getRawValue()).subscribe({
            next: (response) => {
                loading.dismiss();
                this.isSubmitting = false;
                this.router.navigateByUrl(startseiteFuerRolle(response.rolle), {replaceUrl: true});
            },
            error: async () => {
                loading.dismiss();
                this.isSubmitting = false;
                const toast = await this.toastCtrl.create({
                    message: 'Benutzername oder Passwort ist falsch.',
                    duration: 2500,
                    color: 'danger',
                    position: 'top',
                });
                toast.present();
            },
        });
    }
}