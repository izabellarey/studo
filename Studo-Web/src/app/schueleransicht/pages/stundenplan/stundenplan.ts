import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StundenplanSchuelerService } from '../../services/stundenplanschueler.service';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent
} from '@ionic/angular';

@Component({
  selector: 'app-stundenplan',
  templateUrl: './stundenplan.html',
  styleUrls: ['./stundenplan.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    CommonModule
  ]
})

export class SchueleransichtPage implements OnInit {

  constructor(public schuelerService: StundenplanSchuelerService) {}

  ngOnInit() {
    this.schuelerService.ladeStundenplan();
  }

  stundenListe = this.schuelerService.alleStunden;

  wochentage = [
    { key: 'MONTAG', name: 'Montag' },
    { key: 'DIENSTAG', name: 'Dienstag' },
    { key: 'MITTWOCH', name: 'Mittwoch' },
    { key: 'DONNERSTAG', name: 'Donnerstag' },
    { key: 'FREITAG', name: 'Freitag' },
  ];

  //---------------- Tabellenzeilen --------------------------------

  stundennummern = computed(() => {
    const nummern = this.stundenListe().map((s) => s.stundennummer);
    return [...new Set(nummern)].sort((a, b) => a - b);
  });

  stundeFinden(tag: string, nr: number) {
    return this.stundenListe().find((s) => s.wochentag === tag && s.stundennummer === nr);
  }

  uhrzeit(nr: number) {
    const stunde = this.stundenListe().find((s) => s.stundennummer === nr);
    return stunde ? `${stunde.start.slice(0, 5)} - ${stunde.ende.slice(0, 5)}` : '';
  }
}