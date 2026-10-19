import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../../services/konto.service';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonGrid,
  IonRow,
  IonCol,
  IonCard,
  IonCardContent,
  IonAvatar,
  IonBadge,
  IonItem,
  IonLabel,
  IonInput,
  IonButton
} from '@ionic/angular';

@Component({
  selector: 'app-konto',
  templateUrl: './konto.html',
  styleUrls: ['./konto.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonGrid,
    IonRow,
    IonCol,
    IonCard,
    IonCardContent,
    IonAvatar,
    IonBadge,
    IonItem,
    IonLabel,
    IonInput,
    IonButton,
    FormsModule
  ]
})
export class KontoPage implements OnInit{
  accountService = inject(AccountService);

  vornameEntwurf = '';
  nachnameEntwurf = '';
  benutzernameEntwurf = '';

  altesPasswort = '';
  neuesPasswort = '';
  passwortBestaetigen = '';

  ngOnInit() {
    this.accountService.ladeProfil().subscribe({
      next: profil => {
        this.vornameEntwurf = profil.vorname;
        this.nachnameEntwurf = profil.name;
        this.benutzernameEntwurf = profil.benutzername;
      },
      error: err => console.error('Profil konnte nicht geladen werden', err)
    });
  }

  onChangeSpeichern() {
    this.accountService.profilSpeichern(this.vornameEntwurf, this.nachnameEntwurf, this.benutzernameEntwurf).subscribe({
      next: () => console.log('Profil gespeichert'),
      error: err => console.error('Fehler beim Speichern', err)
    });
  }

  onPasswprtAendern() {
    if (this.neuesPasswort !== this.passwortBestaetigen) {
      console.warn('Passwörter stimmen nicht überein');
      return;
    }
    this.accountService.passwortAendern(this.altesPasswort, this.neuesPasswort).subscribe({
      next: () => {
        console.log('Passwort geändert');
        this.altesPasswort = '';
        this.neuesPasswort = '';
        this.passwortBestaetigen = '';
      },
      error: err => console.error('Fehler beim Passwort ändern', err)
    });
  }
}
