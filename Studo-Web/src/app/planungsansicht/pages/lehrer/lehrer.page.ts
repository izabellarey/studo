//Imports
import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  IonHeader, IonToolbar, IonTitle, IonContent, IonIcon,
  IonInput, IonItem, IonSelect, IonSelectOption,
  IonLabel, IonButton, IonDatetime,
  IonToggle,
  IonAccordion,
  IonAccordionGroup
} from '@ionic/angular';

import { addOutline, closeOutline, trashOutline, createOutline, checkmarkCircleOutline, closeCircleOutline } from 'ionicons/icons';
import { addIcons } from 'ionicons';

import {
  TeacherService,
  VerfuegbarkeitFuerLehrkraftDTO as Verfuegbarkeit,
  VerfuegbarkeitsTyp,
  LehrkraftAnzeigenDTO as Lehrkraft,
  LehrkraftAnlegenDTO,
  LehrkraftBearbeitenDTO,
  LerngruppeFuerLehrkraftDTO,
  FachUebersichtDTO,
  Anstellungsverhaeltnis,
  Praeferenz,
  Wochentag,
  VerfuegbarkeitEingabeDTO
} from '../../services/lehrer.service';
import { StammdatenService } from '../../services/stammdaten.service';

//Lehrer Formular erstellen
interface LehrerFormular {
  vorname: string;
  name: string;
  anstellungsverhaeltnis: Anstellungsverhaeltnis | '';
  deputatsstunden: number | null;
  faecher: FachUebersichtDTO[];
  praeferenzen: Praeferenz[];
  verfuegbarkeiten: VerfuegbarkeitEingabeDTO[],
  passwort: string;
  aktiv: boolean;
  kuerzel: string;
  lerngruppe: LerngruppeFuerLehrkraftDTO | null;
  lerngruppeId: number | null; 
}

//Für die Verfügbarkeiten
interface NeuerEintrag {
  wochentag: Wochentag;
  typ: VerfuegbarkeitsTyp;
  zeitslotId: number | null;
  ganztaegig: boolean;
  gueltigVon: string;
  gueltigBis: string;
}

//Filter für die Lehrkräfte anlegen
export type LehrerFilter = 'alle' | 'deaktiviert'

@Component({
  selector: 'app-lehrer',
  templateUrl: './lehrer.html',
  styleUrls: ['./lehrer.css'],
  standalone: true,
  imports: [
    IonHeader, 
    IonToolbar, 
    IonTitle, 
    IonContent, 
    IonIcon,
    IonInput, 
    IonItem, 
    IonSelect, 
    IonSelectOption,
    IonLabel, 
    IonButton, 
    IonDatetime,
    FormsModule, 
    CommonModule, 
    IonToggle,
    IonAccordion,
    IonAccordionGroup,
  ]
})
export class LehrerPage {

  constructor(public teacherService: TeacherService, public stammdatenService: StammdatenService) {
    addIcons({ 
      addOutline, 
      closeOutline, 
      trashOutline, 
      createOutline,
      checkmarkCircleOutline,
      closeCircleOutline 
    });

    //Funktionen Laden
    this.teacherService.faecherLaden();
    this.teacherService.lehrkraftLaden();
    this.teacherService.deaktivierterLehrkraefteLaden();
    this.teacherService.praeferenzLaden();
    this.stammdatenService.stundenrasterLaden();
  }

  //Variablen deklarieren
  wochentage = Object.values(Wochentag);
  stundenraster = this.stammdatenService.stundenrasterSignal;
  verfTyp = VerfuegbarkeitsTyp;

  verfuegbareFaecher = this.teacherService.alleFaecher;
  lehrerListe = this.teacherService.alleLehrkraefte;
  aktiverFilter = signal<LehrerFilter>('alle');

  //Filter setzen
  filterSetzen(filter: LehrerFilter) {
    this.aktiverFilter.set(filter);
  }

  gefilterteListe = computed(() => {
    const filter = this.aktiverFilter();
    if (filter === 'deaktiviert') {
      return this.teacherService.alleDaktivierten();
    }
    return this.teacherService.alleLehrkraefte();
  });

  statusAendern(lehrkraft: Lehrkraft) {
    if (lehrkraft.aktiv) {
      this.teacherService.lehrkraftDeaktivieren(lehrkraft);
    } else {
      this.teacherService.lehrkraftAktivieren(lehrkraft);
    }
  }

  //Popup
  popupIsOpen = false;

  openPopup() {
    this.popupIsOpen = !this.popupIsOpen;
    if (!this.popupIsOpen) {
      this.bearbeitendId = null;
      this.formData = this.leeresFormular();
    }
  }

  //Leeres Formular und leeren Eintrag erstellen
  formData: LehrerFormular = this.leeresFormular();

  private leeresFormular(): LehrerFormular {
    return {
      vorname: '',
      name: '',
      anstellungsverhaeltnis: '',
      deputatsstunden: null,
      faecher: [],
      praeferenzen: [],
      verfuegbarkeiten: [],
      passwort: '',
      aktiv: true,
      kuerzel: '',
      lerngruppe: null,
      lerngruppeId: null
    };
  }

  private leererEintrag(): NeuerEintrag {
    return { 
      wochentag: Wochentag.MONTAG, 
      typ: VerfuegbarkeitsTyp.NEUTRAL, 
      zeitslotId: null, 
      ganztaegig: false, 
      gueltigVon: '', 
      gueltigBis: '' 
    };
  }

  neuerEintrag: NeuerEintrag = this.leererEintrag();

  //Verfügbarkeiten nach Typ filtern
  verfuegbarkeitNachTyp(typ: VerfuegbarkeitsTyp) {
    return this.formData.verfuegbarkeiten.filter(e => e.typ === typ);
  }

  verfuegbarkeitNachTypFuer(lehrer: Lehrkraft, typ: VerfuegbarkeitsTyp): Verfuegbarkeit[] {
    return (lehrer.verfuegbarkeiten ?? []).filter(v => v.typ === typ);
  }

  //Eintrag von Verfügbarkeiten Hinzufügen + Entfernen
  eintragHinzufuegen() {
    const eintrag = this.neuerEintrag;

    if (!eintrag.gueltigVon || !eintrag.gueltigBis) return;
    if (!eintrag.ganztaegig && !eintrag.zeitslotId) return;

    const neuerDto: VerfuegbarkeitEingabeDTO = {
      zeitslotId: eintrag.ganztaegig ? null : eintrag.zeitslotId,
      wochentag: eintrag.wochentag,
      typ: eintrag.typ,
      ganztaegig: eintrag.ganztaegig,
      startdatum: eintrag.gueltigVon.substring(0, 10),
      enddatum: eintrag.gueltigBis.substring(0, 10)
    };

    this.formData.verfuegbarkeiten.push(neuerDto);
    this.neuerEintrag = this.leererEintrag();
  }

  eintragEntfernen(eintrag: VerfuegbarkeitEingabeDTO) {
    this.formData.verfuegbarkeiten = this.formData.verfuegbarkeiten.filter(e => e !== eintrag);
  }

  fachVergleichen(a: FachUebersichtDTO, b: FachUebersichtDTO) {
    return a && b ? a.id === b.id : a === b;
  }

  //Bearbeiten des Formulars bzw. der Lehrkraft
  bearbeitendId: number | null = null;

  bearbeitenStarten(lehrer: Lehrkraft) {
    this.bearbeitendId = lehrer.id;
    this.formData = {
      vorname: lehrer.vorname,
      name: lehrer.name,
      anstellungsverhaeltnis: lehrer.anstellungsverhaeltnis,
      deputatsstunden: lehrer.deputatsstunden,
      faecher: lehrer.faecher.map(f => ({ ...f })), 
      praeferenzen: (lehrer.praeferenzen ?? []).map(p => ({ ...p })),
      verfuegbarkeiten: lehrer.verfuegbarkeiten.map(v => ({
        zeitslotId: v.zeitslotId,
        wochentag: v.wochentag,
        typ: v.typ,
        ganztaegig: v.ganztaegig,
        startdatum: v.startdatum,
        enddatum: v.enddatum
      })),
      passwort: '',
      aktiv: lehrer.aktiv,
      kuerzel: lehrer.kuerzel,
      lerngruppe: lehrer.lerngruppe ? { ...lehrer.lerngruppe } : null,
      lerngruppeId: null
    };
    this.popupIsOpen = true;
  }

  //Lehrkraft speichern
  speichern() {
    if (!this.formData.vorname || !this.formData.name) return;

    const fachIds = this.formData.faecher.map(f => f.id);
    const praeferenzIds = this.formData.praeferenzen.map(p => p.id);

    if (this.bearbeitendId !== null) {
      const daten: LehrkraftBearbeitenDTO = {
        name: this.formData.name,
        vorname: this.formData.vorname,
        aktiv: this.formData.aktiv,
        anstellungsverhaeltnis: this.formData.anstellungsverhaeltnis as Anstellungsverhaeltnis,
        deputatsstunden: this.formData.deputatsstunden ?? 0,
        fachIds,
        praeferenzIds,
        lerngruppeId: this.formData.lerngruppeId,
        verfuegbarkeiten: this.formData.verfuegbarkeiten
      };
      this.teacherService.lehrkraftAktualisieren(this.bearbeitendId, daten);
    } else {
      const daten: LehrkraftAnlegenDTO = {
        name: this.formData.name,
        vorname: this.formData.vorname,
        passwort: this.formData.passwort,
        anstellungsverhaeltnis: this.formData.anstellungsverhaeltnis as Anstellungsverhaeltnis,
        deputatsstunden: this.formData.deputatsstunden ?? 0,
        fachIds,
        praeferenzIds,
        lerngruppeId: this.formData.lerngruppeId,
        verfuegbarkeiten: this.formData.verfuegbarkeiten
      };
      this.teacherService.lehrkraftHinzufuegen(daten);
    }

    this.bearbeitendId = null;
    this.formData = this.leeresFormular();
    this.popupIsOpen = false;
  }
}