import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonButtons,
  IonBackButton,
  IonIcon,
  IonGrid,
  IonRow,
  IonCol,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonModal,
  IonItem,
  IonLabel,
  IonInput,
  IonAlert
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { add, trash, ellipsisVertical } from 'ionicons/icons';
import { LerngruppeService, LerngruppeDto } from '../../services/klassen.service';

  interface NeueKlasse {
    bezeichnung: string;
    klassenlehrer: string;
    klassenraum: string;
  }

@Component({
  selector: 'app-klassen2',
  templateUrl: './klassen2.html',
  styleUrls: ['./klassen2.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonButtons,
    IonBackButton,
    IonIcon,
    IonGrid,
    IonRow,
    IonCol,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
    IonModal,
    IonItem,
    IonLabel,
    IonInput,
    IonAlert,
    RouterLink
  ]
})
export class Klassen2Page implements OnInit, OnDestroy {
  // Kommt aus der Route /klassen/:stufe, z. B. "5"
  stufe = '';

  // Steuert die Sichtbarkeit des "Klasse anlegen"-Modals
  isAddModalOpen = false;

    // Klasse, für die gerade der Deaktivieren-Dialog offen ist (null = Dialog zu)
  klasseZumDeaktivieren: LerngruppeDto | null = null;

  // Buttons des Bestätigungsdialogs
  deaktivierenButtons = [
    { text: 'Abbrechen', role: 'cancel', cssClass: 'alert-btn-abbrechen' },
    {
      text: 'Deaktivieren',
      role: 'destructive',
      cssClass: 'alert-btn-deaktivieren',
      handler: () => this.bestaetigeDeaktivierung()
    }
  ];

  // Formularwerte des Modals
  newKlasse: NeueKlasse = {
    bezeichnung: '',
    klassenlehrer: '',
    klassenraum: ''
    };

  private paramSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private klassenService: LerngruppeService
  ) {
    addIcons({ add, trash, ellipsisVertical });
  }

  ngOnInit() {
    this.paramSub = this.route.paramMap.subscribe(params => {
      this.stufe = params.get('detailansicht') ?? '';
      this.klassenService.ladeJahrgangsstufen();
    this.klassenService.ladeKlassen(Number(this.stufe));
    });
  }

  ngOnDestroy() {
    this.paramSub?.unsubscribe();
  }

  get nummer() {
    return this.klassenService.jahrgangsstufen().find(j => j.id === Number(this.stufe))?.nummer ?? '';
  }

  get klassen(): LerngruppeDto[] {
  return this.klassenService.klassen();}

  //Zeigt die richtige Klassenstufenzahl an (Überschrift)
  get titel(): string {
    return `Klassenstufe ${this.nummer}`;
  }

  //Zeigt die Anzahl und die Klassenstufenzahl an (Unterschrift)
  get untertitel(): string {
    return `Übersicht aller ${this.klassen.length} Parallelklassen der ${this.nummer}. Jahrgangsstufe`;
  }

  goToKlasse(klasse: LerngruppeDto) {
    this.router.navigate(['/planungsansicht/vertretungsplan', this.stufe, klasse.lerngruppeId]);
  }

 openStundenplan(klasse: LerngruppeDto, event: Event) {
  event.stopPropagation();
  this.router.navigate(['/planungsansicht/vertretungsplan', this.stufe, klasse.lerngruppeId]);
  }

  // Öffnet den Bestätigungsdialog.
  // stopPropagation verhindert, dass zusätzlich goToKlasse() der Card ausgelöst wird.
  deaktiviereKlasse(klasse: LerngruppeDto, event: Event) {
    event.stopPropagation();
    this.klasseZumDeaktivieren = klasse;
  }

  // Warntext Klasse deaktivieren
  get deaktivierenNachricht(): string {
    const name = this.klasseZumDeaktivieren?.bezeichnung ?? '';
    return `Möchten Sie die Klasse ${name} wirklich deaktivieren? Dies kann nicht rückgängig gemacht werden.`;
  }

  // Wird nur ausgeführt, wenn im Dialog "Deaktivieren" geklickt wurde
  bestaetigeDeaktivierung() {
    if (this.klasseZumDeaktivieren) {
      this.klassenService.deaktiviereKlasse(this.klasseZumDeaktivieren);
    }
  }

  // Wird beim Schließen des Dialogs aufgerufen (egal welcher Button)
  schliesseDeaktivierenDialog() {
    this.klasseZumDeaktivieren = null;
  }

  openAddModal() {
    this.isAddModalOpen = true;
  }

  closeAddModal() {
    this.isAddModalOpen = false;
  }

addKlasse() {
  const buchstabe = this.newKlasse.bezeichnung.trim().replace(/^\d+/, '').toLowerCase();
  if (!buchstabe || !this.nummer) {
    return;
  }

  this.klassenService.erstelleKlasse(Number(this.stufe), {
    bezeichnung: `${this.nummer}${buchstabe}`
  });
  this.newKlasse = { bezeichnung: '', klassenlehrer: '', klassenraum: '' };
  this.closeAddModal();
}
}