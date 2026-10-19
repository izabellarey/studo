import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { addIcons } from 'ionicons';
import { calendarOutline, closeOutline } from 'ionicons/icons';
import { LehreransichtService, LehrerRaumDto } from '../../services/lehreransicht.service';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonIcon
} from '@ionic/angular';
 
@Component({
  selector: 'app-raeume',
  templateUrl: './raeume.html',
  styleUrls: ['./raeume.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonIcon,
    CommonModule
  ]
})
 
export class RaeumePage implements OnInit {
 
  constructor(public lehrerService: LehreransichtService) {
    addIcons({ calendarOutline, closeOutline });
  }
 
  ngOnInit() {
    this.lehrerService.ladeRaeume();
  }
 
  raumListe = this.lehrerService.alleRaeume;
  raumplan = this.lehrerService.aktuellerRaumplan;
 
  wochentage = [
    { key: 'MONTAG', name: 'Montag' },
    { key: 'DIENSTAG', name: 'Dienstag' },
    { key: 'MITTWOCH', name: 'Mittwoch' },
    { key: 'DONNERSTAG', name: 'Donnerstag' },
    { key: 'FREITAG', name: 'Freitag' },
  ];
 
  //---------------- Popup Raumplan -------------------
 
  popupIsOpen = false;
  ausgewaehlterRaum: LehrerRaumDto | null = null;
 
  raumplanOeffnen(raum: LehrerRaumDto) {
    this.ausgewaehlterRaum = raum;
    this.lehrerService.ladeRaumplan(raum.id);
    this.popupIsOpen = true;
  }
 
  popupSchliessen() {
    this.popupIsOpen = false;
    this.ausgewaehlterRaum = null;
  }
 
  //---------------- Tabellenzeilen Raumplan --------------------------------
 
  zeitslots = this.lehrerService.raumZeitslots;
 
  stundeFinden(tag: string, nr: number) {
    return this.raumplan().find((s) => s.wochentag === tag && s.stundennummer === nr);
  }
}