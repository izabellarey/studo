//Imports
import { Component, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, switchMap } from 'rxjs/operators';

import { IonContent, IonHeader, IonToolbar, IonTitle, IonSelect, IonSelectOption, IonItem, IonButton, IonIcon, IonToggle, IonLabel } from '@ionic/angular';

import { VertretungsplanService, EreignisTyp, Wochentag, AenderungAuswahlDTO, RaumAlternativeDTO, LehrkraftAlternativeDTO, UnterrichtsstundeDTO } from '../../../services/vertretungsplan.service';

//Steuert welche Felder je nach Änderungsgrund bearbeitbar sind
const aenderungsKonfig: Record<string, { lehrerAenderbar: boolean; raumAenderbar: boolean; titel: string }> = {
  [EreignisTyp.AUSFALL]: { lehrerAenderbar: true,  raumAenderbar: false,  titel: 'Lehrerausfall – Vertretung stundeen' },
  [EreignisTyp.RAUMAENDERUNG]: { lehrerAenderbar: false, raumAenderbar: true,  titel: 'Raumsperrung – neuen Raum wählen' },
  [EreignisTyp.ZUSATZSTUNDE]:    { lehrerAenderbar: false,  raumAenderbar: true,  titel: 'Zusatzfach – Raum wählen' }
};

@Component({
  selector: 'app-vertretungsplan',
  templateUrl: './vertretungsplan.html',
  styleUrls: ['./vertretungsplan.css'],
  standalone: true,
  imports: [
    IonContent, 
    IonHeader, 
    IonToolbar, 
    IonTitle,
    IonSelect, 
    IonSelectOption, 
    IonItem, 
    IonIcon, 
    CommonModule, 
    FormsModule
  ]
})
export class VertretungsplanPage {

  //Routing variablen festlegen
  private klassenstufeIdParam = toSignal(this.route.paramMap.pipe(map(p => Number(p.get('klassenstufeId')))));
  private klasseIdParam = toSignal(this.route.paramMap.pipe(map(p => Number(p.get('lerngruppeId')))));
  private aenderungIdParam = toSignal(this.route.queryParamMap.pipe(map(p => { const id = p.get('aenderungId'); return id ? Number(id) : null;})));
  

  //aktuell bearbeitete Stunde
  bearbeitenId: number | null = null;
  popupIsOpen = false;
  bearbeiteteStunde: UnterrichtsstundeDTO | null = null;

  //Auswahl fürs Popup
  bearbeiteterRaum: RaumAlternativeDTO | null = null;
  bearbeiteteLehrkraft: LehrkraftAlternativeDTO | null = null;

  constructor(
    private route: ActivatedRoute,
    public stundenplanService: VertretungsplanService,
  ) {
    
    //Stundenplan für konkrete Klasse laden
    effect(() => {
      const klassenstufeId = this.klassenstufeIdParam();
      const klasseId = this.klasseIdParam();
      const aenderungId = this.aenderungIdParam();
      if (klassenstufeId === undefined || klasseId === undefined) return;

      this.bearbeitenAbbrechen();
      this.stundenplanService.stundenplanLaden(klassenstufeId, klasseId);

      if (aenderungId != null) {
        this.stundenplanService.offeneAenderungLaden(aenderungId);
      }
    })

    
    this.stundenplanService.zeitslotsLaden();
  }

  //Variablen festlegen
  wochentage: Wochentag[] = [Wochentag.MONTAG, Wochentag.DIENSTAG, Wochentag.MITTWOCH, Wochentag.DONNERSTAG, Wochentag.FREITAG];
  stundenplan = this.stundenplanService.alleStundenplaene;
  eintraege = this.stundenplan;
  aenderungsVorschlag = this.stundenplanService.aktuellerAenderungsVorschlag;

  //Zugriff auf die aktuellen IDs für andere Methoden
  get klassenstufeId(): number {
    return this.klassenstufeIdParam() ?? 0;
  }

  get klasseId(): number {
    return this.klasseIdParam() ?? 0;
  }

  stundeFinden(wochentag: Wochentag, stundennummer: number): UnterrichtsstundeDTO | undefined {
    return this.stundenplan().find(stunde => stunde.wochentag === wochentag && stunde.stundennummer === stundennummer);
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
  
  //Funktion um festzulegen, dass nur Stunden, die von einer Änderung betroffen sind bearbeitbar sein sollen
  bearbeitbar(stunde: UnterrichtsstundeDTO): boolean {
    return stunde.grund !== null && stunde.offeneAenderung;
  }

  checkAenderungKonfig(stunde: UnterrichtsstundeDTO) {
    if (!stunde.grund) {
      return null;
    }

    return aenderungsKonfig[stunde.grund] ?? null;
  }

  //Verfügbare Räume und Lehrkräfte finden
  verfuegbareRaeume(): RaumAlternativeDTO[] {
    return this.aenderungsVorschlag() ?.raumalternativen ?? [];
  }

  verfuegbareLehrkraft(): LehrkraftAlternativeDTO[] {
    return this.aenderungsVorschlag() ?.lehrkraftalternativen ?? [];
  }

  findRaumIdByBezeichnung(bezeichnung: string): number {
    const raum = this.verfuegbareRaeume().find((r) => r.bezeichnung === bezeichnung);
    return raum ? raum.raumId : 0; // Standardwert, falls nicht gefunden
  }

  findLehrkraftIdByKuerzel(kuerzel: string): number {
    const lehrkraft = this.verfuegbareLehrkraft().find(
      (l) => `${l.vorname[0]}. ${l.nachname}` === kuerzel
    );
    return lehrkraft ? lehrkraft.lehrkraftId : 0; // Standardwert, falls nicht gefunden
  }

  //Bearbeitung
  bearbeitungStarten(stunde: UnterrichtsstundeDTO) {
    if (!this.bearbeitbar(stunde)) {
      return;
    }

    this.bearbeitenId = stunde.id;
    this.bearbeiteteStunde = stunde;

    this.popupIsOpen = true;

    this.stundenplanService.aenderungsVorschlageLaden(this.klassenstufeId, this.klasseId, stunde.id);
  }

  bearbeitenAbbrechen() {
    this.popupIsOpen = false;

    this.bearbeitenId = null;
    this.bearbeiteteStunde = null;
    this.bearbeiteterRaum = null;
    this.bearbeiteteLehrkraft = null;
  }


  //Vergleich um sicherzustellen, dass nciht zweimal das selbe auftaucht
  raumVergleichen(a: RaumAlternativeDTO, b: RaumAlternativeDTO) {
    return a && b ? a.raumId === b.raumId : a === b;
  }

  lehrkraftVergleichen(a: LehrkraftAlternativeDTO, b: LehrkraftAlternativeDTO) {
    return a && b ? a.lehrkraftId === b.lehrkraftId : a === b;
  }
  
  //Änderungen speichern
  speichern(stunde: UnterrichtsstundeDTO) {

    if (!this.bearbeitbar(stunde)) {
      console.error('Stunde ist nicht bearbeitbar.');
      return;
    }

    const konfig = this.checkAenderungKonfig(stunde);

    if (!konfig) {
      console.error('Keine Konfiguration für diese Änderung gefunden.');
      return;
    }

    if (konfig.raumAenderbar && !this.bearbeiteterRaum) {
      console.error('Bitte einen Raum auswählen.');
      return;
    }

    if (konfig.lehrerAenderbar && !this.bearbeiteteLehrkraft) {
      console.error('Bitte eine Lehrkraft auswählen.');
      return;
    }

    const auswahl: AenderungAuswahlDTO = {
      gewaehlterRaum: konfig.raumAenderbar
        ? this.bearbeiteterRaum
        : null,

      gewaehlteLehrkraft: konfig.lehrerAenderbar
        ? this.bearbeiteteLehrkraft
        : null,
    };

    this.stundenplanService
      .aenderungBestaetigen(
        this.klassenstufeId,
        this.klasseId,
        stunde.id,
        auswahl
      )
    .subscribe({
      next: (daten) => {
        console.log('ÄNDERUNG ERFOLGREICH:', daten);

        this.bearbeitenAbbrechen();
      },

      error: (err) => {
        console.error('ÄNDERUNG FEHLGESCHLAGEN:', err);
        console.error('Status:', err.status);
        console.error('Message:', err.message);
        console.error('Error Body:', err.error);
      }
    });
  }
  }
