//Imports
import { Component, computed, effect, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { StammdatenService, VertretungsprioritaetDTO, PflichtfachvorgabeDTO, StufenvorgabeDTO, MaximalbelastungDTO, PausenregelDTO, FachAnlegenDTO, Raumart } from '../../services/stammdaten.service';

import {
  IonContent,
  IonIcon,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonCol,
  IonGrid,
  IonRow,
  IonItem,
  IonSelect,
  IonSelectOption,
  IonLabel,
  IonInput,
  IonToggle,
  IonList,
  IonRefresher,
  IonReorder,
  IonReorderGroup,
  ReorderEndCustomEvent,
  IonAccordionGroup,
  IonAccordion
} from '@ionic/angular';

import {
  arrowForwardOutline,
  businessOutline,
  calendarClearOutline,
  closeOutline
} from 'ionicons/icons';

import { addIcons } from 'ionicons';

//Festlegung, welche Daten vom Popup / Formular zu erwarten sind
interface FachFormular {
  kuerzel: string;
  name: string;
  benoetigterRaumtyp: Raumart | null;
}


@Component({
  selector: 'app-stammdaten',
  templateUrl: './stammdaten2.html',
  styleUrls: ['./stammdaten2.css'],
  imports: [
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonIcon,
    IonCol,
    IonGrid,
    IonRow,
    IonItem,
    IonSelect,
    IonSelectOption,
    IonInput,
    IonLabel,
    IonList,
    IonAccordion,
    IonAccordionGroup,
    IonToggle,
    IonRefresher,
    IonReorder,
    IonReorderGroup,
    FormsModule,
  ]
})
export class Stammdaten2Page {

  constructor(public stammdatenService: StammdatenService) {
    addIcons({
      arrowForwardOutline,
      businessOutline,
      calendarClearOutline,
      closeOutline
    });

    //Aufruf der Lademethoden
    this.stammdatenService.schulprofilLaden();
    this.stammdatenService.pausenregelLaden();
    this.stammdatenService.pflichtvorgabenLaden();
    this.stammdatenService.faecherLaden();
    this.stammdatenService.vertretungsprioritaetenLaden();
    this.stammdatenService.stundenrasterLaden();
    this.stammdatenService.maximalbelastungLaden();

    effect(() => {
      this.vertretungsprioritaeten = [...this.stammdatenService.alleVertretungsprioritaeten()];
    });
  }

  //Variablen deklarieren
  schulprofil = this.stammdatenService.schulprofilSignal;
  pausenregeln = this.stammdatenService.allePausenregeln;
  pflichtfaecher = this.stammdatenService.alleFaecherAlsPflichtfachvorgaben;
  alleFaecher = this.stammdatenService.alleFaecher;
  stundenraster = this.stammdatenService.stundenrasterSignal;
  maximalbelastungen = this.stammdatenService.gesamteMaximalbelastung;

  vertretungsprioritaeten: VertretungsprioritaetDTO[] = [];

  hauptfaecher = computed(() => this.pflichtfaecher().filter(f => f.hauptfach));
  nichtHauptfaecher = computed(() => this.pflichtfaecher().filter(f => !f.hauptfach));

  //JahrgangsstufenIDs zusammensuchen aus den verschiednene DTOs
  jahrgangsstufenIds = computed(() => {
    const ids = new Set<number>(); //automatische Duplikate verhindern
    this.maximalbelastungen().forEach(mb => ids.add(mb.jahrgangsstufeId));
    this.pausenregeln().forEach(pr => ids.add(pr.jahrgangsstufeId));
    this.pflichtfaecher().forEach(f => f.stufen.forEach(s => ids.add(s.jahrgangsstufeId)));
    return Array.from(ids).sort((a, b) => a - b);
  });

  jahrgangsnummerFuerId(jahrgangsstufeId: number): number {
    for (const fach of this.pflichtfaecher()) {
      const stufe = fach.stufen.find(s => s.jahrgangsstufeId === jahrgangsstufeId);

      if (stufe) {
        return stufe.jahrgangsnummer;
      }
    }
    return jahrgangsstufeId;
  }

  //Maximalbelastung pro Jahrgang heruasfiltern bzw. finden
  maximalbelastungFuerJahrgang(jahrgangsstufeId: number): MaximalbelastungDTO | undefined {
    return this.maximalbelastungen().find(mb => mb.jahrgangsstufeId === jahrgangsstufeId);
  }

  //Pausenregel pro Jahrgang, als Array, weil es mehrere gibt
  pausenregelFuerJahrgang(jahrgangsstufeId: number): PausenregelDTO[]{
    return this.pausenregeln().filter(pr => pr.jahrgangsstufeId === jahrgangsstufeId);
  }

  stufenFuerFach(fach: PflichtfachvorgabeDTO, jahrgangsstufeId: number): StufenvorgabeDTO | undefined {
    return fach.stufen.find(s => s.jahrgangsstufeId === jahrgangsstufeId);
  }

  stufenvorgabeFuerFachSpeichern(fachId: number, stufe: StufenvorgabeDTO) {
    this.stammdatenService.stufenvorgabenAktualisieren(fachId, stufe.id, stufe);
  }

  stufeFuerFachAnlegen(fach: PflichtfachvorgabeDTO, jahrgangsstufeId: number) {
    const dto: StufenvorgabeDTO = {
      id: 0,
      jahrgangsstufeId,
      jahrgangsnummer: this.jahrgangsnummerFuerId(jahrgangsstufeId),
      stundenanzahl: 0,
      doppelstundenBevorzugt: false,
      aktiv: true
    };
    this.stammdatenService.stufenvorgabenHinzufuegen(fach, dto);
  }

  //Vertretungsprioritäten
  reorderPrioritaeten(event: ReorderEndCustomEvent) {
    const [verschobenePrioritaet] = this.vertretungsprioritaeten.splice(event.detail.from, 1);
    this.vertretungsprioritaeten.splice(event.detail.to, 0, verschobenePrioritaet);
    event.detail.complete();
  }

  vertretungsprioritaetenSpeichern() {
    const reihenfolge: VertretungsprioritaetDTO[] = this.vertretungsprioritaeten.map((p, index) => ({
      jahrgangsstufeId: p.jahrgangsstufeId,
      nummer: p.nummer,
      prio: index + 1
    }));

    this.stammdatenService.vertretungsprioritaetenSpeichern(reihenfolge);
  }

  //Popup, um ein neues Fach hinzuzufügen
  popupIsOpen = false;

  openPopup() {
    this.popupIsOpen = !this.popupIsOpen;
    if (!this.popupIsOpen) {
      this.formData = this.leeresFormular();
    }
  }

  //Leeres Formular anlegen
  formData: FachFormular = this.leeresFormular();

  private leeresFormular(): FachFormular {
    return {
      kuerzel: '', name: '', benoetigterRaumtyp: null
    };
  }

  //Speichern
  speichern() {
    if (!this.formData.name || !this.formData.kuerzel || !this.formData.benoetigterRaumtyp) {
      return;
    }

    const daten: FachAnlegenDTO = {
      name: this.formData.name,
      kuerzel: this.formData.kuerzel,
      benoetigterRaumtyp: this.formData.benoetigterRaumtyp
    };
    this.stammdatenService.fachHinzufuegen(daten);

    this.formData = this.leeresFormular();
    this.popupIsOpen = false;
  }
  
}