//Imports
import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  IonHeader, IonToolbar, IonTitle, IonContent, IonIcon,
  IonInput
} from '@ionic/angular';

import { addOutline, closeOutline, trashOutline, createOutline, checkmarkCircleOutline, closeCircleOutline } from 'ionicons/icons';
import { addIcons } from 'ionicons';

import { AccountBearbeitenDTO, Admin, AdminService, AccountAnlegenDTO } from '../../services/admin.services';

//Festlegung, welche Daten vom Popup / Formular zu erwarten sind
interface AdminFormular {
  vorname: string;
  name: string;
  passwort: string;
}

//Filter werte festlegen
export type AdminFilter = 'alle' |  'deaktiviert';

@Component({
  selector: 'app-admins',
  templateUrl: './admins.html',
  styleUrls: ['./admins.css'],
  standalone: true,
  imports: [
    IonHeader, 
    IonToolbar, 
    IonTitle, 
    IonContent, 
    IonIcon,
    IonInput, 
    FormsModule, 
    CommonModule
  ]
})
export class AdminsPage {

  constructor(public adminService: AdminService) {
    addIcons({ addOutline, closeOutline, trashOutline, createOutline, checkmarkCircleOutline, closeCircleOutline });

    //Lade-Funktionen aufrufen, damit die Admins (alle und deaktivierte) angezeigt werden
    this.adminService.adminLaden();
    this.adminService.deaktivierteAdminsLaden();
  }
  

  //Aktivitätsstatus eines Admins
  statusAendern(admin: Admin) {
    if (admin.aktiv) {
      this.adminService.adminDeaktivieren(admin);
    } else {
      this.adminService.adminAktivieren(admin);
    }
  }

  aktiverFilter = signal<AdminFilter>('alle');
  deaktiviertFilter = signal<AdminFilter>('deaktiviert');

  filterSetzen(filter: AdminFilter) {
    this.aktiverFilter.set(filter);
  }

  //Deaktivierte Admins werden nur bei den Deaktivierten angezeigt, nciht unter alle  
  gefilterteListe = computed(() => {
    const filter = this.aktiverFilter();
    if (filter === 'deaktiviert') {
      return this.adminService.alleDaktivierten();
    }
    return this.adminService.alleAdmins();
  });

  //Liste aller Admins
  adminListe = this.adminService.alleAdmins;

  //Popup
  popupIsOpen = false;

  openPopup() {
    this.popupIsOpen = !this.popupIsOpen;
    if (!this.popupIsOpen) {
      this.bearbeitendId = null;
      this.bearbeitendStatusAktiv = true;
      this.formData = this.leeresFormular();
    }
  }

  //Leeres Formular
  formData: AdminFormular = this.leeresFormular();

  private leeresFormular(): AdminFormular {
    return {
      vorname: '',
      name: '',
      passwort: ''
    };
  }

  //Speichern oder bearbeiten
  bearbeitendId: number | null = null;
  bearbeitendStatusAktiv = true;

  bearbeitenStarten(admin: Admin) {
    this.bearbeitendId = admin.id;
    this.bearbeitendStatusAktiv = admin.aktiv;
    this.formData = {
      vorname: admin.vorname,
      name: admin.name,
      passwort: admin.name //platzhalter, bis ich weiß wie ich damit umgehen will
    };
    this.popupIsOpen = true;
  }

  speichern() {
    //Wenn vor und nachname nicht eingetragen sind funktioniert das speichern nciht
    if (!this.formData.vorname || !this.formData.name) {
      return;
    }

    //ansonten je nachdem, ob bearbeitet oder neu gemacht wird die jeweiligen Funktionen aus dem service aufrufen und ausführen
    if (this.bearbeitendId !== null) {
        const daten: AccountBearbeitenDTO = {
          name: this.formData.name,
          vorname: this.formData.vorname,
          aktiv: this.bearbeitendStatusAktiv
        };
        this.adminService.adminAktualisieren(this.bearbeitendId, daten);
      } else {
        const daten: AccountAnlegenDTO = {
          name: this.formData.name,
          vorname: this.formData.vorname,
          passwort: this.formData.passwort,
        };
        this.adminService.adminHinzufuegen(daten);
      }
  
      //alles wieder auf leer setzen
      this.bearbeitendId = null;
      this.formData = this.leeresFormular();
      this.popupIsOpen = false;
  }
  
}