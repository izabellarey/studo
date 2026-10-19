//Imports
import { Component, effect, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  IonContent, 
  IonHeader, 
  IonToolbar, 
  IonTitle,
  IonItem, 
  IonSelect, 
  IonSelectOption,
  IonInput, 
} from '@ionic/angular';

import { arrowForwardOutline, businessOutline, calendarClearOutline, closeOutline } from 'ionicons/icons';
import { addIcons } from 'ionicons';

import { StammdatenService, Bundesland, Schulform } from '../../services/stammdaten.service';

@Component({
  selector: 'app-stammdaten',
  templateUrl: './stammdaten.html',
  styleUrls: ['./stammdaten.css'],
  imports: [
    IonContent, 
    IonHeader, 
    IonToolbar, 
    IonTitle, 
    IonItem, 
    IonSelect, 
    IonSelectOption,
    IonInput, 
    FormsModule
  ]
})

export class StammdatenPage {
  //Router, damit es mit der navigation zur nächsten seite passt
  private router = inject(Router);

  //Variablen für die Enums setzen
  bundeslaender = Object.values(Bundesland);
  schulformen = Object.values(Schulform);

  bundesland: Bundesland | '' = '';
  schulform: Schulform | '' = '';
  name = '';

  constructor(public stammdatenService: StammdatenService) {
    addIcons({ arrowForwardOutline, businessOutline, calendarClearOutline, closeOutline });
    
    //Laden
    this.stammdatenService.schulprofilLaden();

    effect(() => {
      const schulprofil = this.stammdatenService.schulprofilSignal();
      if (schulprofil) {
        this.bundesland = schulprofil.bundesland;
        this.schulform = schulprofil.schulform;
        this.name = schulprofil.name;
      }
    });
  }

  //Speichern, mit verweis auf Stammdaten2
  speichern(): void {
    if (!this.bundesland || !this.schulform || !this.name) {
      return;
    }

    this.stammdatenService.schulprofilSpeichern({
      bundesland: this.bundesland,
      schulform: this.schulform,
      name: this.name
    });

    this.router.navigate(['/planungsansicht/stammdaten2']);
  }
}