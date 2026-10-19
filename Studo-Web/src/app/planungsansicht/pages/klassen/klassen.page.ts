import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonGrid,
  IonRow,
  IonCol
} from '@ionic/angular';
import { LerngruppeService} from '../../services/klassen.service';

@Component({
  selector: 'app-klassen',
  templateUrl: './klassen.html',
  styleUrls: ['./klassen.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonCard,
    IonCardContent,
    IonCardHeader,
    IonCardTitle,
    IonGrid,
    IonRow,
    IonCol,
    RouterLink,
    RouterLinkActive
  ]
})
export class KlassenPage {
  constructor(
    private router: Router,
    private lerngruppeService: LerngruppeService
  ) {}

  //
  ionViewWillEnter() {
  this.lerngruppeService.ladeJahrgangsstufen();
  }

  //Ändert sofort den Stand der angezeigten Klassen bei Änderungen
  anzahlKlassen(stufe: string): number {
    return this.lerngruppeService.jahrgangsstufen()
      .find(j => j.nummer === Number(stufe))?.anzahlKlassen ?? 0;
  }

  // Weiterleitung zur Klassenliste einer Klassenstufe (z. B. '5', '6', ...)
  goToKlassenstufe(stufe: string) {
    const j = this.lerngruppeService.jahrgangsstufen().find(x => x.nummer === Number(stufe));
    if (j) this.router.navigate(['/planungsansicht/klassen', j.id]);
  }
}

