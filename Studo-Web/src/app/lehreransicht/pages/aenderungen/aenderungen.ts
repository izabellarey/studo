import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LehreransichtService } from '../../services/lehreransicht.service';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton
} from '@ionic/angular';
 
export type AenderungsFilter = 'alle' | 'offen' | 'abgeschlossen';
 
@Component({
  selector: 'app-aenderungen',
  templateUrl: './aenderungen.html',
  styleUrls: ['./aenderungen.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    CommonModule
  ]
})
 
export class AenderungenPage implements OnInit {
 
  constructor(public lehrerService: LehreransichtService) {}
 
  ngOnInit() {
    this.lehrerService.ladeAenderungen();
  }
 
  aenderungsListe = this.lehrerService.alleAenderungen;
 
  //Anzeigenamen für die Typen aus dem Backend
  typNamen: Record<string, string> = {
    AUSFALL: 'Ausfall',
    VERTRETUNG: 'Vertretung',
    RAUMAENDERUNG: 'Raumänderung',
    VERLEGUNG: 'Verlegung',
  };
 
  //---------------- Filter --------------------------------
 
  aktiverFilter = signal<AenderungsFilter>('alle');
 
  filterSetzen(filter: AenderungsFilter) {
    this.aktiverFilter.set(filter);
  }
 
  gefilterteListe = computed(() => {
    const filter = this.aktiverFilter();
    const liste = this.aenderungsListe();
 
    if (filter === 'offen') return liste.filter((a) => a.offen);
    if (filter === 'abgeschlossen') return liste.filter((a) => !a.offen);
    return liste;
  });
}