import { Component, OnInit, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonIcon } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { addOutline, createOutline, trashOutline, keyOutline, checkmarkCircleOutline, closeCircleOutline, closeOutline } from 'ionicons/icons';
import { SchuelerService, SchuelerAnzeigenDto, SchuelerAnlegenDto, SchuelerBearbeitenDto } from '../../services/schueler.service'; //erstmal vorläufig, noch anpassen!
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonBreadcrumb,
  IonBreadcrumbs,
  IonButton,
  IonPopover,
  IonInput,
  IonItem,
  IonLabel,
  IonSelect,
  IonSelectOption
} from '@ionic/angular';

interface SchuelerFormular {
  vorname: string;
  name: string;
  klassenId: number | null;
  passwort: string;
  eintrittsdatum: string;
}

export type KlassenFilter = 'alle' | '5-6' | '7-8' | '9-10' | 'deaktiviert';

@Component({
  selector: 'app-schueler',
  templateUrl: './schueler.html',
  styleUrls: ['./schueler.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonBreadcrumb,
    IonBreadcrumbs,
    CommonModule,
    FormsModule,
    IonIcon,
    IonButton,
    IonPopover,
    IonInput, 
    IonItem,
    IonLabel,
    IonSelect,
    IonSelectOption
  ]
})

export class SchuelerPage implements OnInit {

  constructor(public schuelerService: SchuelerService) {
    addIcons({ addOutline, createOutline, trashOutline, keyOutline, checkmarkCircleOutline, closeCircleOutline, closeOutline });
  }

  ngOnInit() {
      this.schuelerService.ladeSchueler();
      this.schuelerService.ladeDeaktivierteSchueler(); 
      this.schuelerService.ladeKlassen();
  }

  schuelerListe = this.schuelerService.alleSchueler;
  klassenListe = this.schuelerService.alleKlassen;

  //---------------- Filter --------------------------------
  
  aktiverFilter = signal<KlassenFilter>('alle');

  filterSetzen(filter: KlassenFilter) {
    this.aktiverFilter.set(filter);
  }
    
  //Erste Zahl aus Klassen (7b) extrahieren 
  private klassenstufe(klasse: string): number | null {
    const treffer = klasse.match(/\d+/);
    return treffer ? parseInt(treffer[0], 10) : null;
  }

  //Deaktivierte Schüler tauchen nur im deaktiviert-Filter auf, sonst nicht 
  gefilterteListe = computed(() => {
    const filter = this.aktiverFilter();
    

    if (filter == 'deaktiviert') {
      return this.schuelerService.alleDaktivierten();
    }

    const aktive = this.schuelerListe();
    if (filter === 'alle') return aktive;

    const bereiche: Record<'5-6' | '7-8' | '9-10', [number, number]> = {
      '5-6': [5, 6],
      '7-8': [7, 8],
      '9-10': [9, 10],
     };
    const [min, max] = bereiche[filter];

     return aktive.filter((s) => {
       const stufe = this.klassenstufe(s.klasse);
       return stufe !== null && stufe >= min && stufe <= max;
    });
  });

  //---------------- Schüler anlegen / Bearbeiten Popover -------------------
 
  popupIsOpen = false;

  openPopup() {
    this.popupIsOpen = !this.popupIsOpen;
    if (!this.popupIsOpen) {
      this.bearbeitenId = null;
      this.bearbeitendAktiv = true;
      this.formData = this.leeresFormular();
    }
  }
  
  formData: SchuelerFormular = this.leeresFormular();

  private leeresFormular(): SchuelerFormular {
    return { vorname: '', name: '', klassenId: null, passwort: '', eintrittsdatum: '' };
  }

  bearbeitenId: number | null = null;
  bearbeitendAktiv = true;
  
  bearbeitenStarten(s: SchuelerAnzeigenDto) {
    this.bearbeitenId = s.id;
    this.bearbeitendAktiv = s.aktiv;
    this.formData = {
      vorname: s.vorname,
      name: s.name,
      klassenId: s.klassenId,
      passwort: '',
      eintrittsdatum: '',
    };
    this.popupIsOpen = true;
  }

  speichern() {
    if (!this.formData.vorname || !this.formData.name || !this.formData.klassenId || this.formData.klassenId <= 0) return;

    if (this.bearbeitenId) {
      const dto: SchuelerBearbeitenDto = {
        vorname: this.formData.vorname,
        name: this.formData.name,
        aktiv: this.bearbeitendAktiv,
        austrittsdatum: null,
        klassenId: this.formData.klassenId,
        };
        this.schuelerService.schuelerAktualisieren(this.bearbeitenId, dto);
      } else {
        if (!this.formData.passwort || !this.formData.eintrittsdatum) return;
        const dto: SchuelerAnlegenDto = {
          vorname: this.formData.vorname,
          name: this.formData.name,
          passwort: this.formData.passwort,
          eintrittsdatum: this.formData.eintrittsdatum,
          klassenId: this.formData.klassenId,
        };
        this.schuelerService.schuelerHinzufuegen(dto);
      }

      this.bearbeitenId = null;
      this.formData = this.leeresFormular();
      this.popupIsOpen = false;
    }
  

  //------------ Status aktiv/ inaktiv --------------------------
  
  statusUmschalten(s: SchuelerAnzeigenDto) {
    if (s.aktiv) {
      this.schuelerService.schuelerDeaktivieren(s);
    } else {
      this.schuelerService.schuelerReaktivieren(s);
    }
  }

  //------------ Passwort zurücksetzen --------------------------------
 
  passwortPopupOffen = false;
  passwortZuruecksetzenSchuelerId: number | null = null;
  neuesPasswortWert = '';

  passwortZuruecksetzenStarten(s: SchuelerAnzeigenDto) {
    this.passwortZuruecksetzenSchuelerId = s.id;
    this.neuesPasswortWert = '';
    this.passwortPopupOffen = true;
  }

  passwortZuruecksetzenBestaetigen() {
    if (!this.passwortZuruecksetzenSchuelerId || !this.neuesPasswortWert) return;
    this.schuelerService.passwortZuruecksetzen(this.passwortZuruecksetzenSchuelerId, this.neuesPasswortWert);
    this.passwortZuruecksetzenSchuelerId = null;
    this.neuesPasswortWert = '';
    this.passwortPopupOffen = false;
  }
}
