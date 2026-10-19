import { Component, OnInit, inject, signal } from '@angular/core';
import { IonContent, IonItem, IonList, IonLabel, IonBadge, IonHeader, IonToolbar, IonTitle } from '@ionic/angular';
import { AenderungDto, AenderungenService } from '../../services/aenderungenschueler.service';

@Component({
  selector: 'app-aenderungen-schueler',
  templateUrl: './aenderungenschueler.html',
  styleUrls: ['./aenderungenschueler.css'],
  standalone: true,
  imports: [IonContent, IonItem, IonList, IonLabel, IonBadge, IonHeader, IonToolbar, IonTitle],
})
export class AenderungenSchuelerPage implements OnInit {
  private service = inject(AenderungenService);

  aenderungen = signal<AenderungDto[]>([]);
  laedt = signal(true);
  fehler = signal(false);

  ngOnInit() {
    this.service.getAenderungen().subscribe({
      next: (daten) => {
        this.aenderungen.set(daten);
        this.laedt.set(false);
      },
      error: (err) => {
        console.error('Fehler:', err);
        this.fehler.set(true);
        this.laedt.set(false);
      },
    });
  }

typKlasse(a: AenderungDto): string {
  if (a.freistunde) {
    return 'karte-rot';
  }
  if (a.typ === 'ZUSATZSTUNDE') {
    return 'karte-blau';
  }
  return 'karte-gelb';
  }
}