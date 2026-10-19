import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonButtons,
  IonIcon,
  IonModal,
  IonItem,
  IonLabel,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonGrid,
  IonRow,
  IonCol,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { add, trash } from 'ionicons/icons';
import { RaumService, Raum, RaumArt, RaumAnlegen, RAUMARTEN } from '../../services/raum.service';

interface NeuerRaum {
  bezeichnung:string;
  art: RaumArt |null ;
  kapazitaet: number|null;
}

@Component({
  selector: 'app-raeume',
  templateUrl: './raeume.html',
  styleUrls: ['./raeume.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonButtons,
    IonIcon,
    IonModal,
    IonItem,
    IonLabel,
    IonInput,
    IonSelect,
    IonSelectOption,
    IonGrid,
    IonRow,
    IonCol,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent
  ]
})
export class RaeumePage {

  raumarten = RAUMARTEN;
  // Steuert die Sichtbarkeit des "Raum hinzufügen"-Modals
  isAddModalOpen = false;

  // Formularwerte des Modals
  private neuerRaum(): NeuerRaum {
    return{bezeichnung:'', art:null, kapazitaet:null}}

  constructor(
    public raumService: RaumService,
    private router: Router
  ) 
  {
    addIcons({ add, trash });
    this.raumService.ladeRaeume();
  }

  openAddModal() {
    this.isAddModalOpen = true;
  }

  closeAddModal() {
    this.isAddModalOpen = false;
    this.modaldata = this.neuerRaum();
  }

  modaldata: NeuerRaum = this.neuerRaum();

  addRaum() {
    if (!this.modaldata.bezeichnung || !this.modaldata.art?.length || !this.modaldata.kapazitaet) {
      return;
}

    const datenspeicher: RaumAnlegen ={bezeichnung: this.modaldata.bezeichnung, art: this.modaldata.art, kapazitaet: this.modaldata.kapazitaet};
    this.raumService.addRaum(datenspeicher);

    this.modaldata = this.neuerRaum();

    this.closeAddModal();
  }

  // Leitet zur Belegungsseite eines Raums weiter
  openRaumbesetzung(raum: Raum) {
    this.router.navigate(['/planungsansicht/raeume', raum.id]);
  }
}