//Imports
import { Component, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DashboardService, EreignisTyp, EreignisAnlegenDTO, VerfuegbareRaeumeAnfragen, LocalDateTime } from '../../services/dashboard.service';
import { TeacherService } from '../../services/lehrer.service';
import { RaumService } from '../../services/raum.service';
import { LerngruppeService } from '../../services/klassen.service';

import { CommonModule } from '@angular/common';

import {
  IonContent,
  IonIcon,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonItem,
  IonSelect,
  IonSelectOption,
  IonDatetime,
  IonInput,
  IonToggle,
  IonLabel,
} from '@ionic/angular';

import {
  arrowForwardOutline,
  businessOutline,
  calendarClearOutline,
  closeOutline,
} from 'ionicons/icons';

import { addIcons } from 'ionicons';

//Ereignis Formular anlegen (alles was gebraucht wird)
interface EreignisFormular {
  typ: EreignisTyp | '';
  startdatum: string;
  enddatum: string;
  ganztag: boolean;
  startzeitId: number | null;
  endzeitId: number | null;
  lehrkraftId: number | null;
  raumId: number | null;
  lerngruppeId: number | null;
  fachId: number | null;
  grund: string;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  imports: [
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonIcon,
    RouterLink,
    RouterLinkActive, 
    IonItem,
    IonSelect,
    IonSelectOption,
    IonDatetime,
    IonInput,
    IonToggle,
    IonLabel,
    FormsModule,
    CommonModule
  ]
})
export class DashboardPage {

  constructor(public dashboardService: DashboardService, public teacherService: TeacherService, public raumService: RaumService, public lerngruppenService: LerngruppeService) {
    addIcons({
      arrowForwardOutline,
      businessOutline,
      calendarClearOutline,
      closeOutline
    });

    //Lademethoden aus service Aufrufen
    this.dashboardService.dashboardLaden();
    this.dashboardService.zeitslotsLaden();
    this.dashboardService.faecherLaden();
    this.dashboardService.lerngruppeLaden();
    this.teacherService.lehrkraftLaden();
    this.raumService.ladeRaeume();
  }

  //Variablen deklarieren
  aenderungen = computed(() => this.dashboard()?.aktuelleAenderungen ?? []);
  anzahlOffen = computed(() => this.dashboard()?.anzahlOffeneEreignisse ?? 0);

  dashboard = this.dashboardService.dashboard;
  zeitslots = this.dashboardService.alleZeitslots;
  faecher = this.dashboardService.alleFaecher;
  verfuegbareRaeume = this.dashboardService.alleVerfuegbarenRaeume;
  lehrerListe = this.teacherService.alleLehrkraefte;
  raumListe = this.raumService.alleRaeume;
  lerngruppenListe = this.dashboardService.alleLerngruppen;
  ereignisTyp = EreignisTyp;

  //Popup
  popupIsOpen = false;

  openPopup() {
    this.popupIsOpen = !this.popupIsOpen;
    if (!this.popupIsOpen) {
      this.formData = this.leeresFormular();
    }
  }

  //Leeres Formular anlegen 
  formData: EreignisFormular = this.leeresFormular();

  private leeresFormular(): EreignisFormular {
    return {
      typ: '',
      startdatum: '',
      enddatum: '',
      ganztag: false,
      startzeitId: null,
      endzeitId: null,
      lehrkraftId: null,
      raumId: null,
      lerngruppeId: null,
      fachId: null,
      grund: ''
    }
  }

  //Funktion Ion-DateTime
  isWeekday = (dateString: string) => {
    const date = new Date(dateString);
    const utcDay = date.getUTCDay();
    return utcDay !== 0 && utcDay !== 6;
  };

  private jetzt(): LocalDateTime {
    return new Date().toISOString().slice(0, 19); // "YYYY-MM-DDTHH:mm:ss"
  }

  //Räume laden, die Verfügbar sind
  verfuegbareRaeumeLaden() {
    const zeitslotId = this.formData.startzeitId;
    const fachId = this.formData.fachId;
    const startdatum = this.formData.startdatum;

    if (zeitslotId === null|| fachId === null|| !startdatum) {
      return;
    }

    const anfrage: VerfuegbareRaeumeAnfragen = {
      zeitslotId: zeitslotId,
      fachId: fachId,
      startdatum: `${startdatum}T00:00:00` //damit wenn es als Raum angezeigt wird die LocalDateTime eingehalten wird
    }

    this.dashboardService.verfuegbareRaeumeAnfragen(anfrage)
  }

  //Ereignis Speichern und anlegen
  ereignisSpeichern() {
    if (!this.formData.typ || !this.formData.startdatum) return;

    let dto: EreignisAnlegenDTO;

    //je nachdem, welcher Ereignistyp angelegt wird andere Daten zurückgeben
    switch (this.formData.typ) {
      //Lehrerausfall
      case EreignisTyp.AUSFALL:
        if (!this.formData.lehrkraftId) return;
        dto = {
          typ: EreignisTyp.AUSFALL,
          startdatum: this.formData.startdatum,
          enddatum: this.formData.enddatum || this.formData.startdatum,
          ganztag: this.formData.ganztag,
          startzeitId: this.formData.ganztag ? null : this.formData.startzeitId,
          endzeitId: this.formData.ganztag ? null : this.formData.endzeitId,
          erstellungsdatum: this.jetzt(),
          lehrkraftId: this.formData.lehrkraftId,
          grund: this.formData.grund
        };
        break;

      //Raumänderung
      case EreignisTyp.RAUMAENDERUNG:
        if (!this.formData.raumId) return;
        dto = {
          typ: EreignisTyp.RAUMAENDERUNG,
          startdatum: this.formData.startdatum,
          enddatum: this.formData.enddatum || this.formData.startdatum,
          ganztag: this.formData.ganztag,
          startzeitId: this.formData.ganztag ? null : this.formData.startzeitId,
          endzeitId: this.formData.ganztag ? null : this.formData.endzeitId,
          erstellungsdatum: this.jetzt(),
          raumId: this.formData.raumId
        };
        break;

      //Zusatzstunden
      case EreignisTyp.ZUSATZSTUNDE:
        if (!this.formData.startzeitId || !this.formData.lerngruppeId || !this.formData.fachId || !this.formData.raumId) return;
        dto = {
          typ: EreignisTyp.ZUSATZSTUNDE,
          startdatum: this.formData.startdatum,
          enddatum: this.formData.enddatum || this.formData.startdatum,
          ganztag: false,
          startzeitId: this.formData.startzeitId,
          lerngruppeId: this.formData.lerngruppeId,
          fachId: this.formData.fachId,
          raumId: this.formData.raumId,
          erstellungsdatum: this.jetzt()
        };
        break;

      default:
        return;
    }

    //Anlegen und das Formular wieder leeren
    this.dashboardService.ereignisAnlegen(dto);
    this.formData = this.leeresFormular();
    this.popupIsOpen = false;
  }

  //Ereignisart herausfinden für HTML
  getEreignisArt(typ: EreignisTyp): string {
    switch (typ) {
      case EreignisTyp.AUSFALL: return 'typ-ausfall';
      case EreignisTyp.RAUMAENDERUNG: return 'typ-raumaenderung';
      case EreignisTyp.ZUSATZSTUNDE: return 'typ-zusatzstunde';
      default: return '';
    }
  }

}