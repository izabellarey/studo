import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { addIcons } from 'ionicons';
import { AlertController } from '@ionic/angular';
import { timer } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';
import { checkmarkCircle, checkmarkCircleOutline } from 'ionicons/icons';
import { IonContent,
         IonHeader,
         IonTitle,
         IonToolbar,
         IonButtons,
         IonButton,
         IonIcon,
         IonAccordionGroup,
         IonAccordion,
         IonItem,
         IonLabel,
         IonList,
         IonBadge,
         IonPopover,
         IonSelect,
         IonSelectOption 
        } from '@ionic/angular';

  import {
    InitstundenplanService,
    KlasseUebersichtDTO,
    StundenplanAnzeigenDTO,
    UnterrichtsstundeDTO,
    UnterrichtsstundeAendernDTO,
    AenderungsVorschlagDTO,
    PlanStatus,
    Wochentag,
    GesamtvarianteNummer
  } from '../../services/initstundenplan.service';      
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-initstundenplan',
  templateUrl: './initstundenplan.page.html',
  styleUrls: ['./initstundenplan.page.scss'],
  imports: [IonContent,
            IonHeader,
            IonTitle, 
            IonToolbar, 
            CommonModule, 
            FormsModule,
            IonButtons,
            IonButtons,
            IonButton,
            IonIcon,
            IonAccordionGroup,
            IonAccordion,
            IonItem,
            IonLabel,
            IonList,
            IonBadge,
            IonPopover,
            IonSelect,
            IonSelectOption
          ]
})
export class InitstundenplanPage   {
  private service = inject(InitstundenplanService);
  
  constructor() { 
    addIcons({ checkmarkCircle, checkmarkCircleOutline });
    this.ladeKlassen();
    this.ladeAktuelleGeneration();
  }

  // Für Template Wochentage als Spalten
  readonly wochentage: Wochentag[] = [
    Wochentag.MONTAG,
    Wochentag.DIENSTAG,
    Wochentag.MITTWOCH,
    Wochentag.DONNERSTAG,
    Wochentag.FREITAG
  ];

  //!!Soll aus Stammdaten (StundenrasterDTO.stundenProTag bzw zeitslots kommen)
  readonly stundenProTag = [1,2,3,4,5,6,7,8];
  readonly varianten: GesamtvarianteNummer[] = [1,2,3];


  //----------------- Generierung -------------------------


private alertController = inject(AlertController);

generierungLaeuft = signal(false);

async stundenplaeneGenerieren(): Promise<void> {
  this.generierungLaeuft.set(true);

  this.service.generierungStarten().subscribe({
    next: async (res) => {
      const alert = await this.alertController.create({
        header: 'Generierung gestartet',
        message: 'Die Generierung der Stundenpläne dauert einige Minuten.',
        buttons: ['OK']
      });
      await alert.present();

      this.generierungStatusPollen(res.generationId);
    },
    error: async () => {
      this.generierungLaeuft.set(false);
      const alert = await this.alertController.create({
        header: 'Fehler',
        message: 'Es läuft bereits eine Generierung oder es ist ein Fehler aufgetreten',
        buttons: ['OK']
      });
      await alert.present();
    }
  });
}

private generierungStatusPollen(generationId: number): void {
  timer(0, 5000).pipe(
    switchMap(() => this.service.getGenerierungStatus(generationId)),
    takeWhile((res) => res.status === 'LAEUFT', true) //true = letzten finalen Wert noch durchlassen
  ).subscribe({
    next: async (res) => {
      if (res.status === 'FERTIG') {
        this.generierungLaeuft.set(false);
        this.generationId.set(generationId);
        const alert = await this.alertController.create({
          header: 'Fertig',
          message: 'Die Stundenpläne wurden erfolgreich generiert.',
          buttons: ['OK']
        });
        await alert.present();
      } else if (res.status === 'FEHLER') {
        this.generierungLaeuft.set(false);
        const alert = await this.alertController.create({
          header: 'Fehler',
          message: 'Bei der Generierung ist ein Fehler aufgetreten.',
          buttons: ['OK']
        });
        await alert.present();
      }
    },
    error: async () => {
      this.generierungLaeuft.set(false);
      const alert = await this.alertController.create({
        header: 'Fehler',
        message: 'Der Generierungsstatus konnte nicht abgerufen werden.',
        buttons: ['OK']
      });
      await alert.present();
    }
  });
}
  
  //----------------- Klassenstufen und Klassen ---------------


  klassen = signal<KlasseUebersichtDTO[]>([]);
  generationId = signal<number | null>(null);


  //----------------- Stundenplan-Varianten pro Klasse ------------------



  private variantenProKlasse = signal<Map<number, Record<GesamtvarianteNummer, StundenplanAnzeigenDTO>>>(new Map());
  
  //Anzeige welche Variante pro Klasse angezeigt wird
  angezeigteVarianteProKlasse = signal<Map<number, GesamtvarianteNummer>>(new Map());


  //---------------- Varianten Global -----------------------------


  gewaehlteVariante = signal<GesamtvarianteNummer | null>(null);
  varianteGewaehlt = computed(() => this.gewaehlteVariante() !== null);

  varianteAnzeigen(klasse: KlasseUebersichtDTO, variante:GesamtvarianteNummer): void {
    this.angezeigteVarianteProKlasse.update((map) => new Map(map).set(klasse.id, variante));
  }

  varianteWaehlen(variante: GesamtvarianteNummer): void {
    this.gewaehlteVariante.set(variante);
  }


  //----------------- Bearbeiten einer einzelnen Unterrichtsstunde -------------
  

  editPopoverOpen = signal(false);
  editEvent = signal<Event | null>(null);
  editKontext = signal<{klasseId: number; stunde: UnterrichtsstundeDTO } | null>(null);
  editVorschlag = signal<AenderungsVorschlagDTO | null>(null);
  editGewaehlteLehrkraftId = signal<number | null>(null);
  editGewaehlterRaumId = signal<number | null>(null);


  //---------------- Finale Stundenpläne veröffentlichen -------------------------


  jahrgangsstufenNummern = computed(() => [...new Set(this.klassen().map((k) => k.jahrgangsnummer))]
  .sort((a,b) => a-b));

  klassenFuer(nummer: number): KlasseUebersichtDTO[] {
    return this.klassen().filter((k) => k.jahrgangsnummer === nummer);
  }

  //Berechnung aller Klassen in den Jahrgangsstufen -> Button bleibt gesperrt, bis alle Klassen aufgeklappt sind 
  gesamtAnzahlKlassen = computed(() => this.klassen().length);


  //------------- Laden -------------------------------


  private ladeKlassen(): void {
    this.service.getKlassen().subscribe((klassen) => this.klassen.set(klassen));
  }

  private ladeAktuelleGeneration(): void {
    this.service.getAktuelleGeneration().subscribe((generationId) => this.generationId.set(generationId));
    //Fehlerfall noch keine Generation abdecken!
  }

  onKlasseGeoeffnet(klasse: KlasseUebersichtDTO): void {
    const generationId = this.generationId();
    if (this.variantenProKlasse().has(klasse.id) || generationId === null) return;
    
    forkJoin({
      1: this.service.getStundenplanFuerVariante(klasse.id, 1, generationId),
      2: this.service.getStundenplanFuerVariante(klasse.id, 2, generationId),
      3: this.service.getStundenplanFuerVariante(klasse.id, 3, generationId)
    }).subscribe((varianten) => {
      this.variantenProKlasse.update((map) => 
        new Map(map).set(klasse.id, varianten as Record<GesamtvarianteNummer, StundenplanAnzeigenDTO>)
      );
      this.angezeigteVarianteProKlasse.update((map) => new Map(map).set(klasse.id, 1));
    });
  }


  angezeigterPlan(klasse: KlasseUebersichtDTO): StundenplanAnzeigenDTO | undefined {
    const variante = this.angezeigteVarianteProKlasse().get(klasse.id);
    return variante ? this.variantenProKlasse().get(klasse.id)?.[variante] : undefined;
  }


  //------------- Einzelne Stunde bearbeiten -------------------------


  stundeBearbeiten(
    event: Event,
    klasseId: number,
    stunde: UnterrichtsstundeDTO
  ): void {
    if (stunde.freistunde) {
      return;
    }

    this.editEvent.set(event);
    this.editKontext.set({ klasseId, stunde});
    this.editGewaehlteLehrkraftId.set(null);
    this.editGewaehlterRaumId.set(null);
    this.editVorschlag.set(null);
    this.editPopoverOpen.set(true);

    this.service.getAenderungsVorschlag(stunde.id).subscribe((vorschlag) => this.editVorschlag.set(vorschlag));
  }

  aenderungSpeichern(): void {
    const kontext = this.editKontext();
    const generationId = this.generationId();
    if (!kontext || generationId === null) {
      return;
    }

    const dto: UnterrichtsstundeAendernDTO = {
      lehrkraftId: this.editGewaehlteLehrkraftId(),
      raumId: this.editGewaehlterRaumId()
    };

    this.service.stundeAendern(kontext.stunde.id, dto).subscribe(() => {
      //nur aktuell angezeigte Variante der Klasse neu laden
      const variante = this.angezeigteVarianteProKlasse().get(kontext.klasseId);
      if (variante) {
        this.service.getStundenplanFuerVariante(kontext.klasseId, variante, generationId).subscribe((plan) => {
          this.variantenProKlasse.update((map) => {
            const neu = new Map(map);
            const bisherige = neu.get(kontext.klasseId);
            if (bisherige) {
              neu.set(kontext.klasseId, {...bisherige, [variante]: plan});
            }
            return neu;
          });
        });
      }    
      this.editPopoverOpen.set(false);
    });  
  }


  //------------- Veröffentlichen ---------------------------
  

  veroeffentlichen(): void {
    const generationId = this.generationId();
    const variante = this.gewaehlteVariante();
    if (generationId === null || variante === null) return;

    this.service.veroeffentlichen(generationId, variante).subscribe(() => {
      //TODO: Erfolgsmeldung + ggf. Navigation nach Veröffentlichung ergänzen
    });
  }


  //------------ Template-Hilfsfunktion ----------------------
  

  stundeAn(plan: StundenplanAnzeigenDTO, tag: Wochentag, nummer: number): UnterrichtsstundeDTO | undefined {
    return plan.unterrichtsstunden.find((s) => s.wochentag === tag && s.stundennummer === nummer);
  }
}
