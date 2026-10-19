import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LehreransichtService } from '../../services/lehreransicht.service';
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
 
export class StundenplanPage implements OnInit {
 
  constructor(public lehrerService: LehreransichtService) {}
 
  ngOnInit() {
    this.lehrerService.ladeStundenplan();
  }
 
  stundenListe = this.lehrerService.alleStunden;
 
  wochentage = [
    { key: 'MONTAG', name: 'Montag' },
    { key: 'DIENSTAG', name: 'Dienstag' },
    { key: 'MITTWOCH', name: 'Mittwoch' },
    { key: 'DONNERSTAG', name: 'Donnerstag' },
    { key: 'FREITAG', name: 'Freitag' },
  ];
 
  //---------------- Tabellenzeilen --------------------------------
 
  zeitslots = this.lehrerService.zeitslots;

  stundeFinden(tag: string, nr: number) {
    return this.stundenListe().find((s) => s.wochentag === tag && s.stundennummer === nr);
  }
}