// Imports
import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import type { OverlayEventDetail } from '@ionic/core';

import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonAlert,
  IonAccordion,
  IonAccordionGroup,
  IonItem
} from '@ionic/angular';

import {
  SchulansichtService,
  Lehrkraefte,
  Lerngruppen,
  RaumDTO,
  ArchivDTO,
  UnterrichtsstundeDTO,
  Wochentag,
  ZeitslotDTO,
  RaumplanDTO
} from '../../services/schulansicht.service';

//Filter festlegen
export type SchulansichtFilter = | 'klassen' | 'lehrkraefte' | 'raeume' | 'archiv';


@Component({
  selector: 'app-schulansicht',
  templateUrl: './schulansicht.html',
  styleUrls: ['./schulansicht.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonAlert,
    FormsModule,
    CommonModule,
    IonAccordion,
    IonAccordionGroup,
    IonItem
  ]
})

export class SchulansichtPage {
  constructor(public schulansichtService: SchulansichtService) {

    //Lademethoden aufrufen
    this.schulansichtService.schulansichtLaden();
    this.schulansichtService.lehrkraftLaden();
    this.schulansichtService.lerngruppenLaden();
    this.schulansichtService.raeumeLaden();
    this.schulansichtService.archivLaden();
    this.schulansichtService.stundenrasterLaden();
  }

  //Variable deklarieren
  wochentage = Object.values(Wochentag);
  aktiverFilter = signal<SchulansichtFilter>('klassen');
  lerngruppen = this.schulansichtService.alleLerngruppen;
  lehrkraefte = this.schulansichtService.alleLehrkraefte;
  raeume = this.schulansichtService.alleRaeume;
  archiv = this.schulansichtService.archiv;
  schuljahr = this.schulansichtService.schulansichten;
  stundenplan = this.schulansichtService.alleStundenplaene;
  raumplan = this.schulansichtService.alleRaumplaene;

  ausgewaehlteLerngruppe = signal<Lerngruppen | null>(null);
  ausgewaehlteLehrkraft = signal<Lehrkraefte | null>(null);
  ausgewaehlterRaum = signal<RaumDTO | null>(null);

  //Filter Setzen
  filterSetzen(filter: SchulansichtFilter) {
    this.aktiverFilter.set(filter);

    this.ausgewaehlteLerngruppe.set(null);
    this.ausgewaehlteLehrkraft.set(null);
    this.ausgewaehlterRaum.set(null);
    this.aktuellenStundenplanLeeren();
  }

  private aktuellenStundenplanLeeren() {}


  //Alert für Schuljahreswechsel
  public alertButtons = [
    {
      text: 'Cancel',
      role: 'cancel',
      handler: () => {
        console.log('Alert canceled');
      },
    },
    {
      text: 'OK',
      role: 'confirm',
      handler: () => {
        console.log('Alert confirmed');
      },
    },
  ];

  setResult(event: CustomEvent<OverlayEventDetail>) {
    if (event.detail.role === 'confirm') {
      this.schulansichtService.jahreswechselDurchfuehren().subscribe({
        next: (daten) => {
          this.schulansichtService.schulansichtLaden();
          this.schulansichtService.lerngruppenLaden();
          this.schulansichtService.archivLaden();
        },
        error: (err) => {console.error('Fehler beim Jahreswechsel:', err)}
      });

      this.schulansichtService.schulansichtLaden();
      this.schulansichtService.lerngruppenLaden();
      this.schulansichtService.archivLaden();
    }
  }

  zeitslots = computed<ZeitslotDTO[]>(() => {
    return this.schulansichtService.aktuellesStundenraster()?.zeitslots ?? [];
  });

  //Stunden und Räume finden
  stundeFinden(wochentag: Wochentag, stundennummer: number): UnterrichtsstundeDTO | undefined {
    return this.stundenplan().find(stunde => stunde.wochentag === wochentag && stunde.stundennummer === stundennummer);
  }

  archivStundeFinden(archiv: ArchivDTO, wochentag: Wochentag, stundennummer: number): UnterrichtsstundeDTO | undefined {
    return archiv.unterrichtsstunde.find(
      stunde => stunde.wochentag === wochentag && stunde.stundennummer === stundennummer
    );
  }

  raumbelegungFinden(wochentag: Wochentag, stundennummer: number): RaumplanDTO | undefined {
    return this.raumplan().find(stunde => stunde.wochentag === wochentag && stunde.stundennummer === stundennummer);
  }

  wochentagAnzeigen(tag: Wochentag): string {
    switch (tag) {
      case Wochentag.MONTAG:
        return 'Montag';
      case Wochentag.DIENSTAG:
        return 'Dienstag';
      case Wochentag.MITTWOCH:
        return 'Mittwoch';
      case Wochentag.DONNERSTAG:
        return 'Donnerstag';
      case Wochentag.FREITAG:
        return 'Freitag';
    }
  }

  //Klasse, Lehrkraft und Raum auswählen
  klasseAuswaehlen(lerngruppe: Lerngruppen) {
    this.ausgewaehlteLerngruppe.set(lerngruppe);
    this.ausgewaehlteLehrkraft.set(null);
    this.ausgewaehlterRaum.set(null);
    this.schulansichtService.stundenplanLerngruppeLaden(
      lerngruppe.id
    );
  }

  lehrkraftAuswaehlen(lehrkraft: Lehrkraefte) {
    this.ausgewaehlteLehrkraft.set(lehrkraft);
    this.ausgewaehlteLerngruppe.set(null);
    this.ausgewaehlterRaum.set(null);
    this.schulansichtService.stundenplanLehrkraftLaden(
      lehrkraft.id
    );
  }

  raumAuswaehlen(raum: RaumDTO) {
    this.ausgewaehlterRaum.set(raum);
    this.ausgewaehlteLerngruppe.set(null);
    this.ausgewaehlteLehrkraft.set(null);
    this.schulansichtService.raumplanLaden(
      raum.id
    );
  }

  ausgewaehlterTitel = computed(() => {
    if (this.ausgewaehlteLerngruppe()) {
      return `Stundenplan ${this.ausgewaehlteLerngruppe()!.bezeichnung}`;
    }

    if (this.ausgewaehlteLehrkraft()) {
      const lehrkraft = this.ausgewaehlteLehrkraft()!;
      return `Stundenplan ${lehrkraft.vorname} ${lehrkraft.nachname}`;
    }

    if (this.ausgewaehlterRaum()) {
      return `Raumplan ${this.ausgewaehlterRaum()!.bezeichnung}`;
    }

    return '';
  });
}